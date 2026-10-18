package dev.lapis256.mekanism_empowered.mixin_impl;

import mekanism.common.tile.base.TileEntityMekanism;

/**
 * 暴露 {@code MachineEnergyContainer#tile}，供无法直接 shadow 父类字段的 mixin 使用。
 */
public interface MachineEnergyContainerAccessor {
    TileEntityMekanism mekanismEmpowered$getTile();
}