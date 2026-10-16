package dev.lapis256.mekanism_empowered.data.provider

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.common.init.MekEmpItems
import mekanism.api.providers.IItemProvider
import net.minecraft.data.PackOutput
import net.minecraftforge.client.model.generators.ItemModelProvider
import net.minecraftforge.common.data.ExistingFileHelper


class MekEmpItemModelProvider(output: PackOutput, helper: ExistingFileHelper) : ItemModelProvider(output, MekanismEmpoweredAPI.MOD_ID, helper) {
    private val parent = mcLoc("item/generated")

    override fun registerModels() {
        MekEmpItems.REGISTRY.allItems.forEach(::add)
    }

    private fun add(item: IItemProvider) {
        val path = item.registryName.path
        withExistingParent(path, parent)
            .texture("layer0", MekanismEmpoweredAPI.rl("item/$path"))
    }
}
