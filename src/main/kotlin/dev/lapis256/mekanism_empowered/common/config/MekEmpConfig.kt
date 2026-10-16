package dev.lapis256.mekanism_empowered.common.config

import dev.lapis256.easy_nest_config.impl.ConfigHelper
import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.core.common.config.MekanismNestConfig
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModLoadingContext
import net.minecraftforge.fml.event.config.ModConfigEvent


object MekEmpConfig {
    private lateinit var helper: ConfigHelper
    private val configs: Map<ForgeConfigSpec, MekanismNestConfig>
        get() = helper.configs.asSequence()
            .filter { it.value is MekanismNestConfig }
            .map { it.key to it.value as MekanismNestConfig }
            .toMap()

    val configValues: List<MekanismNestConfig>
        get() = configs.values.toList()

    fun registerConfigs(context: ModLoadingContext) {
        helper = ConfigHelper(context, MekanismEmpoweredAPI.MOD_NAME_CLEAN)

        helper.registerConfig(MekEmpGeneralConfig, ::MekEmpModConfig)
        helper.registerConfig(MekEmpTierConfig, ::MekEmpModConfig)
    }

    fun onConfigLoad(configEvent: ModConfigEvent) {
        val config = configEvent.config as? MekEmpModConfig ?: return

        if (config.modId == MekanismEmpoweredAPI.MOD_ID) {
            config.clearCache(configEvent)
        }
    }
}
