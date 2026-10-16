package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.core.common.util.TileUpgradeSupportFallbackRegistry
import dev.lapis256.mekanism_empowered.core.mixin_impl.ducks.hasUpgradeSupportOverride
import mekanism.common.block.attribute.AttributeUpgradeSupport
import mekanism.common.block.interfaces.ITypeBlock
import mekanism.common.tile.base.TileEntityMekanism
import net.minecraft.world.level.block.Block
import java.util.EnumSet


object MixinImplTileEntityMekanism {
    @JvmStatic
    fun TileEntityMekanism.applyFallbackSupportedUpgrades(block: Block): Boolean {
        val typeBlock = block as? ITypeBlock ?: return false
        val type = typeBlock.type
        if (type.hasUpgradeSupportOverride) {
            return false
        }

        val attribute = type.get(AttributeUpgradeSupport::class.java) ?: return false
        val newUpgrades = EnumSet.copyOf(attribute.supportedUpgrades)

        newUpgrades.addAll(TileUpgradeSupportFallbackRegistry.collectSupportedUpgrades(this))
        newUpgrades.removeIf { upgrade -> TileUpgradeSupportFallbackRegistry.isUnsupportedUpgrade(this, upgrade) }

        type.add(AttributeUpgradeSupport(newUpgrades))
        type.hasUpgradeSupportOverride = true
        return true
    }
}
