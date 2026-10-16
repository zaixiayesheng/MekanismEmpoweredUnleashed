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
