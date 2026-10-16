package dev.lapis256.mekanism_empowered.client

import dev.lapis256.mekanism_empowered.integration.Integrations
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent


object MekanismEmpoweredClient {
    fun init(modEventBus: IEventBus) {
        Integrations.initClient(modEventBus)

        modEventBus.addListener(::clientSetup)
    }

    private fun clientSetup(event: FMLClientSetupEvent) {
    }
}
