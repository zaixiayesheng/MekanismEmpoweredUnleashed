package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.common.MekanismEmpowered
import dev.lapis256.mekanism_empowered.core.extension.fractionUpgrades
import dev.lapis256.mekanism_empowered.core.extension.getInstalledOrDefault
import dev.lapis256.mekanism_empowered.core.extension.isEnergyMaxed
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.Upgrade
import mekanism.common.tile.interfaces.IUpgradeTile
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


object MixinImplMekanismUtils {
    /**
     * 强化层的有效倍率：配置值会随原版上限 N1 自动稀释
     * （`倍率^(16/N1)`，N1 <= 16 时就是配置值本身），
     * 详见 [UnleashedUtils.empoweredMultiplierBase]。
     */
    private val multiplier: Double
        get() = UnleashedUtils.empoweredMultiplierBase()

    /**
     * 诊断日志开关：JVM 参数 -Dmekanism_empowered.debug=true 时，
     * 在强化层的每个入口打印实际数值（机器类、速度/强化数量、
     * 是否解锁、倍率），用于排查"与其他模组并存时强化不生效"。
     */
    private val debugLog: Boolean = System.getProperty("mekanism_empowered.debug") != null

    @JvmStatic
    fun IUpgradeTile.modifyTicks(original: Double): Double {
        if (!isSpeedMaxed()) {
            if (debugLog) MekanismEmpowered.LOGGER.info(
                "[debug] modifyTicks {} speed={} empowered={} maxed=false original={}",
                javaClass.name, getInstalledOrDefault(Upgrade.SPEED),
                getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_SPEED), original
            )
            return original
        }
        val result = original * multiplier.pow(-fractionUpgrades(MekEmpUpgrade.EMPOWERED_SPEED))
        if (debugLog) MekanismEmpowered.LOGGER.info(
            "[debug] modifyTicks {} speed={} empowered={} maxed=true original={} -> {}",
            javaClass.name, getInstalledOrDefault(Upgrade.SPEED),
            getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_SPEED), original, result
        )
        return result
    }

    @JvmStatic
    fun IUpgradeTile.modifyEnergyPerTick(original: Double): Double {
        val speed = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_SPEED)
        if (!isSpeedMaxed() || speed <= 0) {
            if (debugLog) MekanismEmpowered.LOGGER.info(
                "[debug] modifyEnergyPerTick {} speed(vanilla)={} empoweredSpeed={} gated=true original={}",
                javaClass.name, getInstalledOrDefault(Upgrade.SPEED), speed, original
            )
            return original
        }
        val energy = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_ENERGY)
        // Normalized against the configured tier-2 maximum (8-32, default 16) instead of
        // hardcoded 8, so the energy-neutral invariant holds for every upgrade past the
        // vanilla cap. Merged with Mekanism Unleashed.
        val tierMax = MekEmpUpgrade.EMPOWERED_SPEED.max
        // The Empowered Energy line only cancels part of the extra energy use by default
        // (empoweredEnergyOffsetRate, 0.5), so the Empowered tier costs more energy per
        // operation than the vanilla tier does. Set the rate to 1.0 for the old behaviour.
        val result = original * multiplier.pow(
            (2 * speed - UnleashedUtils.empoweredEnergyOffsetRate() * min(energy, max(speed, tierMax))) / tierMax.toDouble()
        )
        if (debugLog) MekanismEmpowered.LOGGER.info(
            "[debug] modifyEnergyPerTick {} vanillaSpeed={} empoweredSpeed={} empoweredEnergy={} tierMax={} original={} -> {}",
            javaClass.name, getInstalledOrDefault(Upgrade.SPEED), speed, energy, tierMax, original, result
        )
        return result
    }

    /**
     * 大型机器专用能量补偿（只作用于「更多机器」的 meklm 大型机器）。
     *
     * 大型机器的每 tick 操作数来自原生 `2^速度`（16 速度 = 4,194,304 次），
     * 而 Mekanism 的每操作耗能随速度指数上升，结果「操作数 × 单次耗能」
     * 常常超出储能一个数量级以上，机器被容量卡住、能耗栏显示整条储能。
     *
     * 补偿方式：按能量升级数量降低单次耗能，且**不提供配置项**（避免被调到
     * 无限），拉满时上限 1024 倍：
     *
     *     divisor = min(1024, 2^(原版能量数/4 + 强化能量数/8))
     *
     * 16 原版能量 + 16 强化能量时 divisor = 64，单次耗能从 5,000 降到 78，
     * 默认配置下大型机器能跑满速度（4,194,304 次/t），能耗栏从 GB 级降到
     * 百万级；不插能量升级时补偿不生效，保持原生行为。
     */
    @JvmStatic
    fun IUpgradeTile.applyLargeMachineEnergyCompensation(original: Double): Double {
        if (!javaClass.name.startsWith(LARGE_MACHINE_PACKAGE)) {
            return original
        }
        val vanillaEnergy = component.getUpgrades(Upgrade.ENERGY)
        val empoweredEnergy = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_ENERGY)
        if (vanillaEnergy <= 0 && empoweredEnergy <= 0) {
            return original
        }
        val divisor = min(
            LARGE_MACHINE_COMPENSATION_CAP,
            2.0.pow(vanillaEnergy / 2.0 + empoweredEnergy / 2.0)
        )
        val result = max(1.0, original / divisor)
        if (debugLog) MekanismEmpowered.LOGGER.info(
            "[debug] largeMachineEnergy {} vanillaEnergy={} empoweredEnergy={} divisor={} original={} -> {}",
            javaClass.name, vanillaEnergy, empoweredEnergy, divisor, original, result
        )
        return result
    }

    private const val LARGE_MACHINE_PACKAGE = "com.jerry.meklm."
    private const val LARGE_MACHINE_COMPENSATION_CAP = 1.099511627776E12

    @JvmStatic
    fun IUpgradeTile.modifyMaxEnergy(original: Double): Double {
        if (!isEnergyMaxed()) {
            return original
        }
        // 储能倍率与每 tick 能耗倍率同阶（指数同为 1×，不再额外乘 2）：
        // 默认 M=20、强化能量升级插满时容量 ×20，与每 tick 能耗 ×20 相等，
        // 于是新增不变量「满电续航 ×1」。原先的 2× 会让容量到 ×20²=×400，
        // 与 Mekanism Extras 的高阶机器叠加后逼近 Long.MAX —— 玩家反馈的
        // "一部分机器存超多能量"。
        val result = original * multiplier.pow(fractionUpgrades(MekEmpUpgrade.EMPOWERED_ENERGY))
        if (debugLog) MekanismEmpowered.LOGGER.info(
            "[debug] modifyMaxEnergy {} vanillaEnergy={} empoweredEnergy={} original={} -> {}",
            javaClass.name, getInstalledOrDefault(Upgrade.ENERGY),
            getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_ENERGY), original, result
        )
        return result
    }
}
