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
     * 大型机器（meklm）专用能量补偿：按插了几个能量升级降低单次耗能。
     *
     * 大型机器每 tick 操作数来自原生 2^速度（16 速度 = 4,194,304 次），
     * 而 Mekanism 的每操作耗能随速度指数上升，两者相乘常常超过储能几个
     * 数量级，机器于是被容量卡住、能耗栏显示整条储能。这里按能量升级
     * 数量压低单次耗能，倍率固定、不提供配置项：
     *
     *     divisor = min(1024, 2^(原版能量数/4 + 强化能量数/8))
     *
     * 16 原版能量 + 16 强化能量时 divisor = 64，单次耗能从 5,000 降到 78；
     * 不插能量升级时不生效，保持原生行为。
     */
    @JvmStatic
    fun IUpgradeTile.applyLargeMachineEnergyCompensation(original: FloatingLong): FloatingLong {
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
        val result = original.divide(divisor)
        return if (result.smallerThan(FloatingLong.ONE)) FloatingLong.ONE else result
    }

    private const val LARGE_MACHINE_PACKAGE = "com.jerry.meklm."
    private const val LARGE_MACHINE_COMPENSATION_CAP = 1.099511627776E12

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
