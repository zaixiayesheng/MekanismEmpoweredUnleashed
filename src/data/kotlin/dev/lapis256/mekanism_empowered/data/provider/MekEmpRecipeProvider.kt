package dev.lapis256.mekanism_empowered.data.provider

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.common.init.MekEmpItems
import dev.lapis256.mekanism_empowered.core.datagen.EmpoweredShapedRecipeBuilder
import dev.lapis256.mekanism_empowered.core.datagen.MekanismDataShapedRecipeBuilder
import dev.lapis256.mekanism_empowered.core.extension.processedTag
import mekanism.api.providers.IItemProvider
import mekanism.common.registries.MekanismBlocks
import mekanism.common.registries.MekanismItems
import mekanism.common.resource.PrimaryResource
import mekanism.common.resource.ResourceType
import mekanism.common.tags.MekanismTags
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeBuilder
import net.minecraft.data.recipes.RecipeProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.block.Blocks
import net.minecraftforge.common.Tags
import java.util.function.Consumer


class MekEmpRecipeProvider(output: PackOutput) : RecipeProvider(output) {
    override fun buildRecipes(output: Consumer<FinishedRecipe>) {
        upgrade(output, MekEmpItems.AUTO_INSERTER, Blocks.STICKY_PISTON)

        advancedUpgrade(output, MekEmpItems.EMPOWERED_SPEED, PrimaryResource.OSMIUM.processedTag(ResourceType.DUST))
        advancedUpgrade(output, MekEmpItems.EMPOWERED_ENERGY, PrimaryResource.GOLD.processedTag(ResourceType.DUST))
        advancedUpgrade(output, MekEmpItems.FAST_ITEM_EJECT, Blocks.PISTON)
        advancedUpgrade(output, MekEmpItems.FAST_ITEM_INSERT, Blocks.STICKY_PISTON)
        advancedUpgrade(output, MekEmpItems.IO_CAPACITY, MekanismTags.Items.DUSTS_LITHIUM)

        gaugeDropper(output, MekEmpItems.BASIC_GAUGE_DROPPER, MekanismItems.GAUGE_DROPPER, MekanismTags.Items.ALLOYS_BASIC)
        gaugeDropper(output, MekEmpItems.ADVANCED_GAUGE_DROPPER, MekEmpItems.BASIC_GAUGE_DROPPER, MekanismTags.Items.ALLOYS_INFUSED)
        gaugeDropper(output, MekEmpItems.ELITE_GAUGE_DROPPER, MekEmpItems.ADVANCED_GAUGE_DROPPER, MekanismTags.Items.ALLOYS_ELITE)
        gaugeDropper(output, MekEmpItems.ULTIMATE_GAUGE_DROPPER, MekEmpItems.ELITE_GAUGE_DROPPER, MekanismTags.Items.ALLOYS_ATOMIC)
    }

    private fun upgrade(upgrade: IItemProvider) = EmpoweredShapedRecipeBuilder(upgrade)
        .pattern(" g ")
        .pattern("aca")
        .pattern(" g ")
        .define('g', Tags.Items.GLASS)
        .define('a', MekanismTags.Items.ALLOYS_ADVANCED)

    private fun upgrade(output: Consumer<FinishedRecipe>, upgrade: IItemProvider, core: ItemLike) {
        build(output, upgrade(upgrade).define('c', core), upgrade.registryName)
    }

    private fun upgrade(output: Consumer<FinishedRecipe>, upgrade: IItemProvider, core: TagKey<Item>) {
        build(output, upgrade(upgrade).define('c', core), upgrade.registryName)
    }

    private fun advancedUpgrade(upgrade: IItemProvider) = EmpoweredShapedRecipeBuilder(upgrade)
        .pattern("hgh")
        .pattern("aca")
        .pattern("hgh")
        .define('h', MekanismItems.HDPE_SHEET)
        .define('g', MekanismBlocks.STRUCTURAL_GLASS)
        .define('a', MekanismTags.Items.ALLOYS_ATOMIC)

    private fun advancedUpgrade(output: Consumer<FinishedRecipe>, upgrade: IItemProvider, core: TagKey<Item>) {
        build(output, advancedUpgrade(upgrade).define('c', core), upgrade.registryName)
    }

    private fun advancedUpgrade(output: Consumer<FinishedRecipe>, upgrade: IItemProvider, core: ItemLike) {
        build(output, advancedUpgrade(upgrade).define('c', core), upgrade.registryName)
    }

    private fun gaugeDropper(output: Consumer<FinishedRecipe>, gaugeDropper: IItemProvider, before: ItemLike, alloy: TagKey<Item>) {
        build(output, MekanismDataShapedRecipeBuilder(gaugeDropper)
            .pattern("a")
            .pattern("b")
            .define('a', alloy)
            .define('b', before),
            gaugeDropper.registryName)
    }

    private fun build(output: Consumer<FinishedRecipe>, builder: RecipeBuilder, id: ResourceLocation) {
        builder.save(output, MekanismEmpoweredAPI.rl(id.path))
    }
}
