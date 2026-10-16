package dev.lapis256.mekanism_empowered.core.datagen

import net.minecraft.advancements.Advancement
import net.minecraft.advancements.AdvancementRewards
import net.minecraft.advancements.RequirementsStrategy
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger
import net.minecraft.core.Holder
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.data.recipes.RecipeCategory
import net.minecraft.data.recipes.ShapedRecipeBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.crafting.CraftingBookCategory
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.crafting.RecipeSerializer
import net.minecraft.world.level.ItemLike
import java.util.function.Consumer


open class EmpoweredShapedRecipeBuilder(stack: ItemStack, val category: RecipeCategory = RecipeCategory.MISC) :
    ShapedRecipeBuilder(category, stack.item, stack.count) {

    constructor(item: ItemLike, amount: Int = 1, category: RecipeCategory = RecipeCategory.MISC) : this(ItemStack(item, amount), category)

    constructor(holder: Holder<Item>, amount: Int = 1, category: RecipeCategory = RecipeCategory.MISC) : this(holder.value(), amount, category)

    override fun save(recipeOutput: Consumer<FinishedRecipe>, id: ResourceLocation) {
        ensureValid(id)

        if (advancement.criteria.isNotEmpty()) {
            advancement.parent(ROOT_RECIPE_ADVANCEMENT).addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id)).requirements(RequirementsStrategy.OR)
        }

        recipeOutput.accept(
            EmpoweredResult(
                id,
                result,
                count,
                group ?: "",
                determineBookCategory(category),
                rows,
                key,
                advancement,
                id.withPrefix("recipes/${category.folderName}/"),
                showNotification,
                recipeSerializer
            )
        )
    }

    override fun ensureValid(id: ResourceLocation) {
        try {
            super.ensureValid(id)
        } catch (e: IllegalStateException) {
            if (e.message != "No way of obtaining recipe $id") {
                throw e
            }
        }
    }

    protected open val recipeSerializer: RecipeSerializer<*> = RecipeSerializer.SHAPED_RECIPE

    open class EmpoweredResult(
        id: ResourceLocation,
        result: Item,
        count: Int,
        group: String,
        category: CraftingBookCategory,
        pattern: MutableList<String>,
        key: MutableMap<Char?, Ingredient?>,
        private val advancement: Advancement.Builder?,
        private val advancementId: ResourceLocation?,
        showNotification: Boolean,
        val serializer: RecipeSerializer<*>,
    ) : Result(id, result, count, group, category, pattern, key, advancement, advancementId, showNotification) {

        override fun getAdvancementId() = advancementId
        override fun serializeAdvancement() = if(advancement?.criteria?.isNotEmpty() == true) advancement.serializeToJson() else null
        override fun getType() = serializer
    }
}
