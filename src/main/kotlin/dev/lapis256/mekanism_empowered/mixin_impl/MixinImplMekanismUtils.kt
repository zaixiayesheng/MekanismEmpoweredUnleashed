package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.common.MekanismEmpowered
import dev.lapis256.mekanism_empowered.core.extension.fractionUpgrades
import dev.lapis256.mekanism_empowered.core.extension.getInstalledOrDefault
import dev.lapis256.mekanism_empowered.core.extension.isEnergyMaxed
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.Upgrade
import mekanism.common.tile.base.TileEntityMekanism
import mekanism.common.tile.interfaces.IUpgradeTile
import kotlin.math.floor
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
     * 目标：把「每 tick 总耗能」压在 int 上限（2,147,483,647 FE/t）以内，
     * 同时让操作数跑到它自己的天花板，而不是按能量升级数无脑打折。
     *
     * 每 tick 总耗能 = 操作数 × 单次耗能；操作数上限由速度线决定：
     *
     *     operationsCap = baseOperations × min(原生 2^速度 + 强化叠层, int 安全余量)
     *     单次耗能上限 = Int.MAX / operationsCap
     *
     * 插满 32 速度 + 32 强化速度时 operationsCap = 1,073,741,696，
     * 单次耗能被压到 2 FE，整机正好以 2,147,483,646 FE/t 的上限跑满；
     * 16 速度时 operationsCap = 4,194,304，单次耗能上限 512 FE。
     * 补偿只为「跑得起来」，不提供配置项，也不会把电费压到免费。
     */
    @JvmStatic
    fun IUpgradeTile.applyLargeMachineEnergyCompensation(original: Double): Double {
        val tile = this as? TileEntityMekanism ?: return original
        return largeMachineEnergyPerTickCap(tile, original)
    }

    /** 大型机器每操作耗能的上限：按这台机器自己罐容量能跑出的操作数反推，保证「操作数 × 单价 ≤ Int.MAX」。 */
    @JvmStatic
    fun largeMachineEnergyPerTickCap(tile: TileEntityMekanism, original: Double): Double {
        if (!tile.javaClass.name.startsWith(LARGE_MACHINE_PACKAGE)) {
            return original
        }
        // 每台大型机器的输入罐容量不同，单 tick 最大操作数也就不同：
        // 回旋 1000 万、电解分离 2400 万、灌注 500 万、颜料混合 1000、太阳能 1000 万。
        // 单价按自己这台机器的上限反推，满罐时正好 21.4 亿 FE/t，且永远不会越过 int。
        val tankOperations = tile.largeMachineTankOperations()
        if (tankOperations <= 0.0) {
            return original
        }
        val operations = max(1.0, min(tile.largeMachineOperationsPerTick(), tankOperations))
        // 抵消率只压缩单价，速度（操作数）不受影响；单价是整数 FE，最低 1。
        val vanillaEnergy = tile.component.getUpgrades(Upgrade.ENERGY)
        // 强化能量同样要原版能量插满才生效，和小机器的解锁规则一致。
        val empoweredEnergy = if (tile.isEnergyMaxed()) tile.getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_ENERGY) else 0
        val offsetProgress = min(
            1.0,
            (UnleashedUtils.vanillaEnergyOffsetRate() * vanillaEnergy +
                UnleashedUtils.empoweredEnergyOffsetRate() * empoweredEnergy) /
                (2.0 * UnleashedUtils.maxUpgrades())
        )
        val perOperationCap = max(1.0, floor(Int.MAX_VALUE.toDouble() / operations * (1.0 - offsetProgress)))
        val result = max(1.0, min(original, perOperationCap))
        if (debugLog) MekanismEmpowered.LOGGER.info(
            "[debug] largeMachineEnergy {} operations={} offsetProgress={} perOperationCap={} original={} -> {}",
            tile.javaClass.name, operations, offsetProgress, perOperationCap, original, result
        )
        return result
    }

    private const val LARGE_MACHINE_PACKAGE = "com.jerry.meklm."

    /**
     * 各台大型机器输入罐容量换算出的单 tick 最大操作数（每操作消耗 1 mB）。
     * 未知机型返回 0，表示不做补偿，保持原生行为。
     */
    private fun TileEntityMekanism.largeMachineTankOperations(): Double = when (javaClass.simpleName) {
        "TileEntityLargeRotaryCondensentrator" -> 10_000_000.0
        "TileEntityLargeElectrolyticSeparator" -> 24_000_000.0
        "TileEntityLargeChemicalInfuser" -> 5_000_000.0
        "TileEntityLargePigmentMixer" -> 1_000.0
        "TileEntityLargeSolarNeutronActivator" -> 10_000_000.0
        else -> 0.0
    }

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
