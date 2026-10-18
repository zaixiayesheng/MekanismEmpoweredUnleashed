package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.common.MekanismEmpowered
import mekanism.common.capabilities.energy.MachineEnergyContainer
import mekanism.common.tile.base.TileEntityMekanism
import mekanism.common.tile.component.TileComponentUpgrade


object MixinImplMachineEnergyContainer {
    @JvmStatic
    fun TileEntityMekanism.modifyUpdateMaxEnergyTarget(original: Boolean) = original || supportsUpgrade(MekEmpUpgrade.EMPOWERED_ENERGY)

    @JvmStatic
    fun TileComponentUpgrade.modifyUpdateEnergyPerTickTarget(original: Boolean) =
        original || supports(MekEmpUpgrade.EMPOWERED_ENERGY) || supports(MekEmpUpgrade.EMPOWERED_SPEED)

    /**
     * 能耗不能超过储能上限。
     *
     * Mekanism 用 `storedEnergy / energyPerTick` 算这一 tick 能跑几次操作，一旦这个除法的结果是 0，
     * 它会同时挂上「能量不足运行」和「能量不足以最高速度运行」两个错误，而只要同时出现这两条就强制停机。
     * 大型机器在高升级数下会因为整数溢出算出天文数字的能耗，正好踩中这条路——把能耗夹到容量以内，
     * 最坏情况也只是降速运行，不会再假死。
     */
    @JvmStatic
    fun clampEnergyPerTick(container: MachineEnergyContainer<*>) {
        val maxEnergy = container.maxEnergy
        if (maxEnergy > 0 && container.energyPerTick > maxEnergy) {
            container.energyPerTick = maxEnergy
        }
    }

    /**
     * FixedUsageEnergyContainer（大型电解分离机等）自己算每 tick 能耗，不经过 MekanismUtils.getEnergyPerTick，
     * 因此大型机器的能耗补偿要在它这里补一次。
     */
    @JvmStatic
    fun applyFixedUsageCompensation(container: MachineEnergyContainer<*>, tile: TileEntityMekanism) {
        val current = container.energyPerTick
        val capped = MixinImplMekanismUtils.largeMachineEnergyPerTickCap(tile, current.toDouble())
        if (capped < current) {
            container.energyPerTick = capped.toLong()
        }
    }
}
