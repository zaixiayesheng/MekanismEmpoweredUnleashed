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
