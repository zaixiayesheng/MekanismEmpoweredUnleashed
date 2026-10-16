package dev.lapis256.mekanism_empowered.unleashed

import dev.lapis256.mekanism_empowered.common.config.MekEmpGeneralConfig
import dev.lapis256.mekanism_empowered.core.extension.getInstalledOrDefault
import mekanism.api.Upgrade
import mekanism.common.config.MekanismConfig
import mekanism.common.tile.interfaces.IUpgradeTile
import mekanism.common.tile.machine.TileEntityDigitalMiner
import mekanism.common.tile.machine.TileEntityElectricPump
import mekanism.common.tile.machine.TileEntityFluidicPlenisher
import mekanism.common.tile.machine.TileEntityFormulaicAssemblicator
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * 数值公式工具集：合并自 Mekanism Unleashed (WhitePhant0m)。
 *
 * 这里集中了"提升原版升级上限（可配置 8-32）"所需的全部公式，
 * 供 [dev.lapis256.mekanism_empowered.mixin.unleashed.MixinMekanismUtils]
 * 以定点注入的方式替换 Mekanism 原版 `MekanismUtils` 中的对应计算。
 *
 * 1.20.1 适配说明：1.20.1 的 `IUpgradeTile#getComponent()` 是可空方法
 * （1.21.1 是平台类型字段），所以原版升级数量的读取统一走
 * [getInstalledOrDefault]（内部已做空判断），不直接碰 `component`。
 */
object UnleashedUtils {

    /** 两道升级线上限的默认值（也是强化倍率稀释的基准）。 */
    private const val DEFAULT_UPGRADE_MAX = 16

    /** 强化倍率配置未加载时的回退值。 */
    private const val DEFAULT_EMPOWERED_MULTIPLIER = 20.0

    /** 原版能量升级抵消率未加载时的回退值（1.0 = 原版行为）。 */
    private const val DEFAULT_VANILLA_ENERGY_OFFSET = 1.0

    /** 强化能量升级抵消率未加载时的回退值（0.5 = 默认只抵一半）。 */
    private const val DEFAULT_EMPOWERED_ENERGY_OFFSET = 0.5

    /**
     * 这些机器不走"合成器"路径（它们有各自专属的 Mixin 处理每 tick 补次），
     * 其余机器统一按合成器钳制最少 1 tick。
     */
    private val NON_CRAFTER_CLASSES: Set<Class<*>> = setOf(
        TileEntityDigitalMiner::class.java,
        TileEntityFluidicPlenisher::class.java,
        TileEntityFormulaicAssemblicator::class.java,
        TileEntityElectricPump::class.java
    )

    fun isCrafter(tile: IUpgradeTile): Boolean = tile.javaClass !in NON_CRAFTER_CLASSES

    /**
     * 速度时间系数：每插 1 个原版速度升级，单次操作耗时
     * × 倍率^(-1/8)（即每个升级约提速 33%，与原版节奏一致）。
     *
     * 注意这是**绝对式**（除以固定的 8，而不是除以当前上限）：
     * 上限被抬到 16/32 之后，每个速度升级仍然按原版力度提速，
     * 这正是"解锁更高的升级上限"的实际收益来源。
     */
    fun time(tile: IUpgradeTile): Double =
        MekanismConfig.general.maxUpgradeMultiplier.get().toDouble().pow(tile.getInstalledOrDefault(Upgrade.SPEED) / -8.0)

    /**
     * 每 tick 能耗的指数部分。
     *
     * 原版公式为 `(2*speed - energy) / 8`，但 Unleashed 原版把能量项
     * 封顶在 `max(8, speed)`——导致"能量升级超过 8 个不再减耗"的问题。
     * 这里把封顶值改为**跟随配置上限**（默认 16，范围 8-32），
     * 保证插满能量升级时每一个都真实计入减耗。
     *
     * 能量中性的关键：速度数 == 能量数、且抵消率为 1.0 时，指数恰好抵消
     * [time] 的指数，即"每操作能耗 = 基础值 ×1"，提速只体现在吞吐上。
     *
     * 抵消率（[vanillaEnergyOffsetRate]，默认 1.0）用来调难度：调低即
     * 能量升级抵不掉那么多额外耗能，机器变得更费电。
     */
    fun energyExponent(tile: IUpgradeTile): Double {
        val speed = tile.getInstalledOrDefault(Upgrade.SPEED)
        val energy = tile.getInstalledOrDefault(Upgrade.ENERGY)
        val maxUpgrades = maxUpgrades()
        return (2 * speed - vanillaEnergyOffsetRate() * min(energy, max(maxUpgrades, speed))) / 8.0
    }

    /** 每 tick 能耗系数（绝对式的指数形式，供 Mixin 直接乘基础值）。 */
    fun energy(tile: IUpgradeTile): Double =
        MekanismConfig.general.maxUpgradeMultiplier.get().toDouble().pow(energyExponent(tile))

    /**
     * 能量容量系数：每插 1 个原版能量升级，容量 × 倍率^(1/8)。
     */
    fun capacity(tile: IUpgradeTile): Double =
        MekanismConfig.general.maxUpgradeMultiplier.get().toDouble().pow(tile.getInstalledOrDefault(Upgrade.ENERGY) / 8.0)

    /**
     * 配置安全读取：**原版**能量升级的抵消率（回退默认 1.0），理由同 [maxUpgrades]。
     *
     * 1.0 = 原版行为；0.0 = 能量升级完全不抵消耗能。
     */
    fun vanillaEnergyOffsetRate(): Double = try {
        MekEmpGeneralConfig.vanillaEnergyOffsetRate.coerceIn(0.0, 1.0)
    } catch (e: IllegalStateException) {
        DEFAULT_VANILLA_ENERGY_OFFSET
    }

    /**
     * 配置安全读取：**强化**能量升级的抵消率（回退默认 0.5），理由同 [maxUpgrades]。
     *
     * 默认 0.5：强化能量插满也只抵掉一半额外耗能，因此强化层默认不再是
     * 能量中性的；填 1.0 可恢复"每操作总能耗 ×1"。
     */
    fun empoweredEnergyOffsetRate(): Double = try {
        MekEmpGeneralConfig.empoweredEnergyOffsetRate.coerceIn(0.0, 1.0)
    } catch (e: IllegalStateException) {
        DEFAULT_EMPOWERED_ENERGY_OFFSET
    }

    /**
     * 配置安全读取：升级上限。
     *
     * SERVER 类型配置在注册表初始化阶段**尚未加载**，
     * 而 `Upgrade#getMax()` 可能在那个阶段就被调用（例如盔甲材质
     * 注册、工具提示缓存构建），直接读配置会抛
     * `IllegalStateException: Cannot get config value before config is loaded`。
     * 因此这里捕获异常并回退到配置默认值 16；游戏内（配置已加载）
     * 的读取不受影响，正常返回玩家配置值。
     */
    fun maxUpgrades(): Int = try {
        max(8, min(32, MekEmpGeneralConfig.maxUpgrades))
    } catch (e: IllegalStateException) {
        DEFAULT_UPGRADE_MAX
    }

    /**
     * 配置安全读取：**强化**升级线的上限（默认 16，与 [maxUpgrades] 独立），
     * 理由同 [maxUpgrades]。
     *
     * 它只决定强化层的总加成被拆成几级；插满时的总加成由
     * [empoweredMultiplierBase] 与原版上限共同决定。
     */
    fun maxEmpoweredUpgrades(): Int = try {
        max(1, min(32, MekEmpGeneralConfig.maxEmpoweredUpgrades))
    } catch (e: IllegalStateException) {
        DEFAULT_UPGRADE_MAX
    }

    /**
     * 强化层的**有效**倍率：随原版上限 N1 自动稀释（以默认上限 16 为基准）。
     *
     * - `N1 <= 16`：[MekEmpGeneralConfig.maxUpgradeMultiplier] 原值（默认 20）；
     * - `N1 > 16`：`倍率^(16/N1)`，例如倍率 20、上限 32 时为 `20^0.5 ≈ 4.47`。
     *
     * 只压不抬：原版上限低于默认值时**不**放大强化层，否则"原版插得越少、
     * 总速度反而越高"。于是总倍率随 N1 单调上升，且 N1=32 时为
     * `10^4 × 4.47 ≈ 4.47 万`（旧行为是 `10^4 × 20 = 20 万`）。
     */
    fun empoweredMultiplierBase(): Double {
        val base = try {
            max(1, MekEmpGeneralConfig.maxUpgradeMultiplier).toDouble()
        } catch (e: IllegalStateException) {
            return DEFAULT_EMPOWERED_MULTIPLIER
        }
        val vanillaMax = maxUpgrades()
        if (vanillaMax <= DEFAULT_UPGRADE_MAX) return base
        return base.pow(DEFAULT_UPGRADE_MAX.toDouble() / vanillaMax.toDouble())
    }

    /**
     * 配置安全读取：Meka 装备附魔开关（回退默认 false），
     * 理由同 [maxUpgrades]。
     */
    fun enchantableMekaGear(): Boolean = try {
        MekEmpGeneralConfig.enchantableMekaGear
    } catch (e: IllegalStateException) {
        false
    }

    /**
     * 配置安全读取：每 tick 补次上限（回退默认 128），理由同 [maxUpgrades]。
     *
     * 泵/数字矿机/流体装填器/公式装配器这四台机器走各自的补次 Mixin，
     * 速度升级插满时理论需求可达数十万次/tick，远超此上限——它就是
     * "再插速度升级没有可见收益"的那条线。
     */
    fun maxOperationsPerTick(): Int {
        val configured = try {
            max(1, min(10000, MekEmpGeneralConfig.maxOperationsPerTick))
        } catch (e: IllegalStateException) {
            128
        }
        if (!autoScaleOperationsPerTick()) return configured

        // 自动跟随 maxUpgrades：原版速度线插满 N 个时的理论需求是 倍率^(N/8)，
        // 补次上限若低于它，多出来的速度升级就完全看不到收益。
        // 只抬高、不压低（想强制压低就把 autoScaleOperationsPerTick 关掉）。
        val demanded = try {
            MekanismConfig.general.maxUpgradeMultiplier.get().toDouble().pow(maxUpgrades() / 8.0)
        } catch (e: IllegalStateException) {
            return configured
        }
        return max(configured, min(10000, demanded.toInt()))
    }

    /**
     * 配置安全读取：补次上限是否自动跟随 [maxUpgrades]（回退默认 true），
     * 理由同 [maxUpgrades]。
     */
    fun autoScaleOperationsPerTick(): Boolean = try {
        MekEmpGeneralConfig.autoScaleOperationsPerTick
    } catch (e: IllegalStateException) {
        true
    }

    /**
     * 第二层（强化）升级的真实效果倍率：插了 n 个强化升级时为 `倍率^(n / 上限)`。
     *
     * 与方块侧 `MixinImplMekanismUtils` 的 `fractionUpgrades(EMPOWERED_*)` 计算、
     * 以及物品侧 `UpgradeBasedUnsignedLongCache` 的指数完全一致，
     * 供升级提示（tooltip）显示真实数值。
     */
    fun empoweredMultiplier(tile: IUpgradeTile, upgrade: Upgrade): Double {
        val max = upgrade.max.toDouble()
        if (max <= 0) return 1.0
        return empoweredMultiplierBase().pow(tile.getInstalledOrDefault(upgrade).toDouble() / max)
    }

    /**
     * 把数值格式化为"有效数字 + 科学计数法"的显示字符串，
     * 用于升级效果提示（例如 2.5E3）。
     */
    fun exponential(d0: Double): String {
        val significant = 4
        val exp = floor(log10(d0)).toInt()
        var d = d0 * 10.0.pow(-exp)
        d = Math.round(d * 10.0.pow(significant - 1)).toDouble() / 10.0.pow(significant - 1)
        val dt = Math.round(d * 10.0.pow(significant - 1)).toDouble() / 10.0.pow(significant - 1 - exp)
        return if (abs(exp) <= significant - 1) dt.toString() else "$d" + "E$exp"
    }
}
