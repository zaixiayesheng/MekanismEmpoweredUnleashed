package dev.lapis256.mekanism_empowered.common.config

import dev.lapis256.easy_nest_config.api.NestConfig
import dev.lapis256.mekanism_empowered.core.common.config.MekanismNestConfig
import net.neoforged.fml.config.ModConfig


object MekEmpGeneralConfig : MekanismNestConfig(ModConfig.Type.SERVER, "general") {
    val maxUpgradeMultiplier by MekEmpConfigTranslations.GENERAL_UPGRADE_MULTIPLIER.applyToBuilder(builder)
        .defineInRange("maxUpgradeMultiplier", 20, 1, Int.MAX_VALUE).cached()

    // Merged from Mekanism Unleashed (WhitePhant0m): the vanilla Speed/Energy cap.
    // It also decides when the Empowered lines unlock: a line must be full first.
    val maxUpgrades by MekEmpConfigTranslations.GENERAL_UPGRADE_MAX.applyToBuilder(builder)
        .defineInRange("maxUpgrades", 16, 8, 32).cached()

    // The Empowered Speed/Energy lines have their own cap now, so the two tiers are
    // configured independently. It only decides how many steps the Empowered bonus is
    // split into; the bonus at full comes from maxUpgradeMultiplier, diluted by
    // maxUpgrades (see UnleashedUtils.empoweredMultiplierBase).
    val maxEmpoweredUpgrades by MekEmpConfigTranslations.GENERAL_EMPOWERED_UPGRADE_MAX.applyToBuilder(builder)
        .defineInRange("maxEmpoweredUpgrades", 16, 1, 32).cached()

    // Offset rate: how much of the extra energy use caused by Speed upgrades the Energy
    // upgrades cancel. 1.0 is the vanilla behaviour (as many Energy upgrades as Speed
    // upgrades = base energy per operation); lower it to make the line thirstier.
    // Packs/servers use this as a difficulty knob.
    val vanillaEnergyOffsetRate by MekEmpConfigTranslations.GENERAL_VANILLA_ENERGY_OFFSET.applyToBuilder(builder)
        .defineInRange("vanillaEnergyOffsetRate", 1.0, 0.0, 1.0).cached()

    // Same knob for the Empowered Energy line. It defaults to 0.5, so a full Empowered
    // Energy line only cancels half of what the Empowered Speed line costs - the Empowered
    // tier is deliberately not energy-neutral. Set it to 1.0 to restore the old behaviour.
    val empoweredEnergyOffsetRate by MekEmpConfigTranslations.GENERAL_EMPOWERED_ENERGY_OFFSET.applyToBuilder(builder)
        .defineInRange("empoweredEnergyOffsetRate", 0.5, 0.0, 1.0).cached()

    val enchantableMekaGear by MekEmpConfigTranslations.GENERAL_ENCHANTABLE_MEKA_GEAR.applyToBuilder(builder)
        .define("enchantableMekaGear", false).cached()

    // Merged from Mekanism Unleashed (WhitePhant0m): the four machines with their own
    // catch-up loop (Pump, Digital Miner, Fluidic Plenisher, Formulaic Assemblicator)
    // were hardcoded to 128 operations per tick, which silently swallowed every Speed
    // upgrade past that point. Configurable now, default stays 128.
    val maxOperationsPerTick by MekEmpConfigTranslations.GENERAL_MAX_OPERATIONS_PER_TICK.applyToBuilder(builder)
        .defineInRange("maxOperationsPerTick", 128, 1, 10000).cached()

    // When enabled (default) the cap above is raised automatically (never lowered) to keep
    // up with maxUpgrades, so raising maxUpgrades alone is enough to keep Speed upgrades
    // meaningful on those four machines. Disable it to use maxOperationsPerTick as an
    // absolute ceiling.
    val autoScaleOperationsPerTick by MekEmpConfigTranslations.GENERAL_AUTO_SCALE_OPERATIONS.applyToBuilder(builder)
        .define("autoScaleOperationsPerTick", true).cached()

    @NestConfig
    @ConfigTranslation(MekEmpConfigTranslations.GENERAL_AUTO_INSERTER)
    object AutoInsert {
        val itemRate by MekEmpConfigTranslations.GENERAL_AUTO_INSERTER_ITEM_RATE.applyToBuilder(builder)
            .defineInRange("itemRate", 8, 1, 64).cached()
        val chemicalRate by MekEmpConfigTranslations.GENERAL_AUTO_INSERTER_CHEMICAL_RATE.applyToBuilder(builder)
            .defineInRange("chemicalRate", 128L, 1L, Long.MAX_VALUE).cached()
        val fluidRate by MekEmpConfigTranslations.GENERAL_AUTO_INSERTER_FLUID_RATE.applyToBuilder(builder)
            .defineInRange("fluidRate", 128, 1, Int.MAX_VALUE).cached()
        val energyRate by MekEmpConfigTranslations.GENERAL_AUTO_INSERTER_ENERGY_RATE.applyToBuilder(builder)
            .defineInRange("energyRate", 128L, 1L, Long.MAX_VALUE).cached()
    }

    override fun getTranslation() = "General Config"
}
