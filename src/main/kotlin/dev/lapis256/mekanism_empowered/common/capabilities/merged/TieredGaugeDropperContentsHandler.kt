package dev.lapis256.mekanism_empowered.common.capabilities.merged

import dev.lapis256.mekanism_empowered.common.config.MekEmpTierConfig
import dev.lapis256.mekanism_empowered.core.common.util.IntLongSupplier
import mekanism.api.NBTConstants
import mekanism.api.chemical.ChemicalTankBuilder
import mekanism.api.fluid.IExtendedFluidTank
import mekanism.api.fluid.IMekanismFluidHandler
import mekanism.api.tier.BaseTier
import mekanism.common.capabilities.DynamicHandler
import mekanism.common.capabilities.chemical.dynamic.DynamicChemicalHandler
import mekanism.common.capabilities.chemical.variable.RateLimitChemicalTank
import mekanism.common.capabilities.fluid.item.RateLimitFluidHandler
import mekanism.common.capabilities.merged.MergedTank
import mekanism.common.capabilities.merged.MergedTankContentsHandler
import mekanism.common.capabilities.resolver.BasicCapabilityResolver
import mekanism.common.capabilities.resolver.ICapabilityResolver
import mekanism.common.util.ItemDataUtils
import net.minecraft.core.Direction
import net.minecraft.world.item.ItemStack
import net.minecraftforge.common.capabilities.ForgeCapabilities
import net.minecraftforge.fluids.capability.IFluidHandlerItem
import java.util.function.Consumer


class TieredGaugeDropperContentsHandler(tier: BaseTier) : MergedTankContentsHandler<MergedTank>(), IMekanismFluidHandler, IFluidHandlerItem {
    private val fluidTanks: MutableList<IExtendedFluidTank>

    init {
        val (reteValue, capacityValue) = when (tier) {
            BaseTier.BASIC -> MekEmpTierConfig.GaugeDropper.basicRate to MekEmpTierConfig.GaugeDropper.basicCapacity
            BaseTier.ADVANCED -> MekEmpTierConfig.GaugeDropper.advancedRate to MekEmpTierConfig.GaugeDropper.advancedCapacity
            BaseTier.ELITE -> MekEmpTierConfig.GaugeDropper.eliteRate to MekEmpTierConfig.GaugeDropper.eliteCapacity
            BaseTier.ULTIMATE -> MekEmpTierConfig.GaugeDropper.ultimateRate to MekEmpTierConfig.GaugeDropper.ultimateCapacity
            else -> error("Invalid tier: $tier")
        }

        val rate = IntLongSupplier(reteValue)
        val capacity = IntLongSupplier(capacityValue)

        mergedTank = MergedTank.create(
            RateLimitFluidHandler.RateLimitFluidTank(rate, capacity, this),
            RateLimitChemicalTank.RateLimitGasTank(
                rate,
                capacity,
                ChemicalTankBuilder.GAS.alwaysTrueBi,
                ChemicalTankBuilder.GAS.alwaysTrueBi,
                ChemicalTankBuilder.GAS.alwaysTrue,
                null,
                DynamicChemicalHandler.DynamicGasHandler(
                    { gasTanks },
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    { onContentsChanged(NBTConstants.GAS_TANKS, gasTanks) }).also { gasHandler = it }),
            RateLimitChemicalTank.RateLimitInfusionTank(
                rate,
                capacity,
                ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                ChemicalTankBuilder.INFUSION.alwaysTrueBi,
                ChemicalTankBuilder.INFUSION.alwaysTrue,
                DynamicChemicalHandler.DynamicInfusionHandler(
                    { infusionTanks },
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    { onContentsChanged(NBTConstants.INFUSION_TANKS, infusionTanks) }).also { infusionHandler = it }),
            RateLimitChemicalTank.RateLimitPigmentTank(
                rate,
                capacity,
                ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                ChemicalTankBuilder.PIGMENT.alwaysTrueBi,
                ChemicalTankBuilder.PIGMENT.alwaysTrue,
                DynamicChemicalHandler.DynamicPigmentHandler(
                    { pigmentTanks },
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    { onContentsChanged(NBTConstants.PIGMENT_TANKS, pigmentTanks) }).also { pigmentHandler = it }),
            RateLimitChemicalTank.RateLimitSlurryTank(
                rate,
                capacity,
                ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                ChemicalTankBuilder.SLURRY.alwaysTrueBi,
                ChemicalTankBuilder.SLURRY.alwaysTrue,
                DynamicChemicalHandler.DynamicSlurryHandler(
                    { slurryTanks },
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    DynamicHandler.InteractPredicate.ALWAYS_TRUE,
                    { onContentsChanged(NBTConstants.SLURRY_TANKS, slurryTanks) }).also { slurryHandler = it })
        )
        this.fluidTanks = mutableListOf(mergedTank.fluidTank)
    }

    override fun load() {
        super.load()
        ItemDataUtils.readContainers(stack, NBTConstants.FLUID_TANKS, getFluidTanks(null))
    }

    override fun getFluidTanks(side: Direction?): MutableList<IExtendedFluidTank> {
        return fluidTanks
    }

    override fun onContentsChanged() {
        onContentsChanged(NBTConstants.FLUID_TANKS, fluidTanks)
    }

    override fun getContainer(): ItemStack = stack

    override fun gatherCapabilityResolvers(consumer: Consumer<ICapabilityResolver>) {
        super.gatherCapabilityResolvers(consumer)
        consumer.accept(BasicCapabilityResolver.constant(ForgeCapabilities.FLUID_HANDLER_ITEM, this))
    }
}
