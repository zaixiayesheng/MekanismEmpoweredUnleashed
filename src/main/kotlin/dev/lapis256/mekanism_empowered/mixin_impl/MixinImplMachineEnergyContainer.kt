package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
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
     * Mekanism 用 `storedEnergy / energyPerTick` 算这一 tick 能跑几次操作，一旦这个除法的
     * 结果是 0，它会同时挂上「能量不足运行」和「能量不足以最高速度运行」两个错误，而只要
     * 同时出现这两条就强制停机。大型机器在高升级数下会算出天文数字的能耗正好踩中这条路
     * ——把能耗夹到容量以内，最坏情况也只是降速运行，不会再假停。
     */
    @JvmStatic
    fun clampEnergyPerTick(container: MachineEnergyContainer<*>) {
        val maxEnergy = container.maxEnergy
        if (!maxEnergy.isZero && container.energyPerTick.greaterThan(maxEnergy)) {
            container.energyPerTick = maxEnergy
        }
    }
}
