package dev.lapis256.mekanism_empowered.integration.mods

import dev.lapis256.mekanism_empowered.common.factory.FactoryBlockResolver
import dev.lapis256.mekanism_empowered.common.factory.FactoryBlockResolverRegistry
import dev.lapis256.mekanism_empowered.common.factory.FactoryTypeKey
import dev.lapis256.mekanism_empowered.common.factory.addDeferredSupportedForFactory
import dev.lapis256.mekanism_empowered.common.init.MekEmpUpgrades.ITEM_IN_OUT_MACHINE_UPGRADES
import dev.lapis256.mekanism_empowered.core.common.util.AdditionalUpgradeUtil.addDeferredSupported
import dev.lapis256.mekanism_empowered.integration.ModIntegration
import fr.iglee42.evolvedmekanism.registries.EMBlockTypes
import fr.iglee42.evolvedmekanism.registries.EMFactoryType
import mekanism.common.registries.MekanismBlockTypes
import mekanism.common.util.EnumUtils
import net.minecraftforge.eventbus.api.IEventBus


internal object EvoMek : ModIntegration {
    override val modId = "evolvedmekanism"

    object FactoryTypeKeys {
        val ALLOYING = FactoryTypeKey.of(modId, "alloying")
    }

    private val factoryResolver by lazy {
        FactoryBlockResolver(
            name = modId,
            types = mapOf(FactoryTypeKeys.ALLOYING to { EMFactoryType.ALLOYING }),
            tiers = { EnumUtils.FACTORY_TIERS.asIterable() },
            resolver = { tier, type -> MekanismBlockTypes.getFactory(tier, type) },
        )
    }

    override fun initCommon(modEventBus: IEventBus) {
        FactoryBlockResolverRegistry.register(factoryResolver)

        registerSupportedUpgrades()
    }

    private fun registerSupportedUpgrades() {
        addDeferredSupportedForFactory(FactoryTypeKeys.ALLOYING, *ITEM_IN_OUT_MACHINE_UPGRADES)

        addDeferredSupported({ EMBlockTypes.ALLOYER }, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupported({ EMBlockTypes.CHEMIXER }, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupported({ EMBlockTypes.MELTER }, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupported({ EMBlockTypes.SOLIDIFIER }, *ITEM_IN_OUT_MACHINE_UPGRADES)
    }
}
