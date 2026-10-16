package dev.lapis256.mekanism_empowered.common.config

import dev.lapis256.easy_nest_config.api.Comment
import dev.lapis256.easy_nest_config.api.NestConfig
import dev.lapis256.mekanism_empowered.core.common.config.MekanismNestConfig
import net.minecraftforge.fluids.FluidType
import net.minecraftforge.fml.config.ModConfig


object MekEmpTierConfig : MekanismNestConfig(ModConfig.Type.SERVER, "tiers") {

    @NestConfig
    @Comment("Configurable Gauge Dropper")
    object GaugeDropper {
        val basicRate = builder.comment("Configurable basic rate for Basic Gauge Dropper")
            .defineInRange("basicRate", 256 * 3, 1, Int.MAX_VALUE).cached()
        val basicCapacity = builder.comment("Configurable basic capacity for Basic Gauge Dropper")
            .defineInRange("basicCapacity", 16 * FluidType.BUCKET_VOLUME * 3, 1, Int.MAX_VALUE).cached()
        val advancedRate = builder.comment("Configurable advanced rate for Advanced Gauge Dropper")
            .defineInRange("advancedRate", 256 * 5, 1, Int.MAX_VALUE).cached()
        val advancedCapacity = builder.comment("Configurable advanced capacity for Advanced Gauge Dropper")
            .defineInRange("advancedCapacity", 16 * FluidType.BUCKET_VOLUME * 5, 1, Int.MAX_VALUE).cached()
        val eliteRate = builder.comment("Configurable elite rate for Elite Gauge Dropper")
            .defineInRange("eliteRate", 256 * 7, 1, Int.MAX_VALUE).cached()
        val eliteCapacity = builder.comment("Configurable elite capacity for Elite Gauge Dropper")
            .defineInRange("eliteCapacity", 16 * FluidType.BUCKET_VOLUME * 7, 1, Int.MAX_VALUE).cached()
        val ultimateRate = builder.comment("Configurable ultimate rate for Ultimate Gauge Dropper")
            .defineInRange("ultimateRate", 256 * 9, 1, Int.MAX_VALUE).cached()
        val ultimateCapacity = builder.comment("Configurable ultimate capacity for Ultimate Gauge Dropper")
            .defineInRange("ultimateCapacity", 16 * FluidType.BUCKET_VOLUME * 9, 1, Int.MAX_VALUE).cached()
    }
}
