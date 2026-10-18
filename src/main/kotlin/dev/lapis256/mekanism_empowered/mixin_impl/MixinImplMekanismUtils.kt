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
import mekanism.common.tile.base.TileEntityMekanism
import mekanism.common.tile.interfaces.IUpgradeTile
import kotlin.math.floor
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
        val tile = this as? TileEntityMekanism ?: return original
        val capped = FloatingLong.create(largeMachineEnergyPerTickCap(tile, Double.MAX_VALUE))
        return if (original.greaterThan(capped)) capped else original
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
        return min(original, max(1.0, floor(Int.MAX_VALUE.toDouble() / operations * (1.0 - offsetProgress))))
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
    fun modifyItemStackMaxEnergy(original: FloatingLong, energyInstalled: Double, upgrades: Map<Upgrade, Int>?): FloatingLong {
        upgrades ?: return original
        if (energyInstalled < Upgrade.ENERGY.max) {
            return original
        }
        val installed = upgrades[MekEmpUpgrade.EMPOWERED_ENERGY] ?: return original
        return modifyEnergy(original, installed / MekEmpUpgrade.EMPOWERED_ENERGY.max.toDouble())
    }
}
