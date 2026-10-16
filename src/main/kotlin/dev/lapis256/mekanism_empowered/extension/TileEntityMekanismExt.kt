package dev.lapis256.mekanism_empowered.extension

import dev.lapis256.mekanism_empowered.mixin.common.tile.AccessorTileEntityMekanism
import mekanism.common.tile.base.TileEntityMekanism


private val TileEntityMekanism.accessor get() = this as? AccessorTileEntityMekanism

val TileEntityMekanism.itemHandlerManager get() =
    accessor?.itemHandlerManager

val TileEntityMekanism.gasHandlerManager get() =
    accessor?.gasHandlerManager

val TileEntityMekanism.infusionHandlerManager get() =
    accessor?.infusionHandlerManager

val TileEntityMekanism.pigmentHandlerManager get() =
    accessor?.pigmentHandlerManager

val TileEntityMekanism.slurryHandlerManager get() =
    accessor?.slurryHandlerManager

val TileEntityMekanism.fluidHandlerManager get() =
    accessor?.fluidHandlerManager
