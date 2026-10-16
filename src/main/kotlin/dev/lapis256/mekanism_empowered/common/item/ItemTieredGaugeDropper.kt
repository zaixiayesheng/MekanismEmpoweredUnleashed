package dev.lapis256.mekanism_empowered.common.item

import dev.lapis256.mekanism_empowered.common.capabilities.merged.TieredGaugeDropperContentsHandler
import mekanism.api.text.TextComponentUtil
import mekanism.api.tier.BaseTier
import mekanism.common.capabilities.ItemCapabilityWrapper
import mekanism.common.item.ItemGaugeDropper
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
import net.minecraft.world.item.ItemStack


class ItemTieredGaugeDropper(val tier: BaseTier, properties: Properties) : ItemGaugeDropper(properties) {
    override fun getName(stack: ItemStack): Component = TextComponentUtil.build(tier.color, super.getName(stack))

    override fun gatherCapabilities(capabilities: MutableList<ItemCapabilityWrapper.ItemCapability>, stack: ItemStack, nbt: CompoundTag?) {
        capabilities.add(TieredGaugeDropperContentsHandler(tier))
    }
}
