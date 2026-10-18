package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.extension.fractionUpgrades
import dev.lapis256.mekanism_empowered.core.extension.getInstalledOrDefault
import dev.lapis256.mekanism_empowered.core.extension.isEnergyMaxed
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import dev.lapis256.mekanism_empowered.core.extension.times
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.Upgrade
import mekanism.api.math.FloatingLong
import mekanism.common.tile.interfaces.IUpgradeTile
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


object MixinImplMekanismUtils {
    /** 与 Unleashed 同一套倍率基准（第二层按 Empowered 上限归一），不直接读原始配置值。 */
    private val multiplier: Double
        get() = UnleashedUtils.empoweredMultiplierBase()

    @JvmStatic
    fun IUpgradeTile.modifyTicks(original: Double): Double {
        if (!isSpeedMaxed()) {
            return original
        }
        return original * multiplier.pow(-fractionUpgrades(MekEmpUpgrade.EMPOWERED_SPEED))
    }

    @JvmStatic
    fun IUpgradeTile.modifyEnergyPerTick(original: FloatingLong): FloatingLong {
        val speed = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_SPEED)
        if (!isSpeedMaxed() || speed <= 0) {
            return original
        }
        val energy = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_ENERGY)
        val tierMax = MekEmpUpgrade.EMPOWERED_SPEED.max
        return original * multiplier.pow(
            (2 * speed - UnleashedUtils.empoweredEnergyOffsetRate() * min(energy, max(speed, tierMax))) / tierMax.toDouble()
        )
    }

    /** 储能倍率与每 tick 能耗倍率同阶：指数同为 1×，这里不再额外乘 2（旧版的 2× 会让容量到 ×20²）。 */
    private fun modifyEnergy(original: FloatingLong, fraction: Double) = original * multiplier.pow(fraction)

    @JvmStatic
    fun IUpgradeTile.modifyTileMaxEnergy(original: FloatingLong): FloatingLong {
        if (!isEnergyMaxed()) {
            return original
        }
        return modifyEnergy(original, fractionUpgrades(MekEmpUpgrade.EMPOWERED_ENERGY))
    }

    /**
     * 大型机器（meklm）专用能量补偿：把「每 tick 总耗能」压在 int 上限以内。
     *
     * 每 tick 总耗能 = 操作数 × 单次耗能；操作数上限由速度线决定：
     *
     *     operationsCap = baseOperations × min(原生 2^速度 + 强化叠层, int 安全余量)
     *     单次耗能上限 = Int.MAX / operationsCap
     *
     * 插满 32 速度 + 32 强化速度时 operationsCap = 1,073,741,696，单次耗能压到
     * 2 FE，整机正好以 2,147,483,646 FE/t 的上限跑满；不提供配置项。
     */
    @JvmStatic
    fun IUpgradeTile.applyLargeMachineEnergyCompensation(original: FloatingLong): FloatingLong {
        if (!javaClass.name.startsWith(LARGE_MACHINE_PACKAGE)) {
            return original
        }
        val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
        val empoweredSpeed = getInstalledOrDefault(MekEmpUpgrade.EMPOWERED_SPEED)
        val baseOperations = 4.0 * max(1, vanillaSpeed)
        val nativeBaseline = 2.0.pow(vanillaSpeed.toDouble())
        val boosted = if (isSpeedMaxed() && empoweredSpeed > 0) {
            nativeBaseline + 2.0 * 2.0.pow(empoweredSpeed.toDouble())
        } else {
            nativeBaseline
        }
        val safeBaseline = Int.MAX_VALUE.toDouble() / baseOperations / 2.0
        val operationsCap = min(boosted, safeBaseline) * baseOperations
        val perOperationCap = Int.MAX_VALUE.toDouble() / max(1.0, operationsCap)
        val capped = FloatingLong.create(max(1.0, perOperationCap))
        return if (original.greaterThan(capped)) capped else original
    }

    private const val LARGE_MACHINE_PACKAGE = "com.jerry.meklm."

    @JvmStatic
    fun modifyItemStackMaxEnergy(original: FloatingLong, energyInstalled: Double, upgrades: Map<Upgrade, Int>?): FloatingLong {
        upgrades ?: return original
        if (energyInstalled < Upgrade.ENERGY.max) {
            return original
        }
        val installed = upgrades[MekEmpUpgrade.EMPOWERED_ENERGY] ?: return original
        return modifyEnergy(original, installed / MekEmpUpgrade.EMPOWERED_ENERGY.max.toDouble())
    }
}
