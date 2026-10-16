package dev.lapis256.mekanism_empowered.common.config

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import mekanism.common.config.IConfigTranslation
import net.minecraft.Util


enum class MekEmpConfigTranslations(path: String, private val title: String, private val tooltip: String, private val button: String?) : IConfigTranslation {
    GENERAL_UPGRADE_MULTIPLIER("general.misc.upgrade_multiplier", "Max Upgrade Multiplier",
          "Base factor of the Empowered Speed/Energy upgrades (default 20). Above Max Upgrades 16 the value is diluted to multiplier^(16/maxUpgrades), so speed and storage cannot run away with the vanilla cap."),
    GENERAL_UPGRADE_MAX("general.misc.upgrade_max", "Max Upgrades",
          "Maximum number of Speed/Energy upgrades a machine can accept (8-32, default 16). The Empowered lines unlock once the vanilla line is full."),
    GENERAL_EMPOWERED_UPGRADE_MAX("general.misc.empowered_upgrade_max", "Max Empowered Upgrades",
          "Maximum number of Empowered Speed/Energy upgrades a machine can accept (1-32, default 16). It only decides how many steps the Empowered bonus is split into; the bonus at full comes from Max Upgrade Multiplier."),
    GENERAL_VANILLA_ENERGY_OFFSET("general.misc.vanilla_energy_offset_rate", "Vanilla Energy Offset Rate",
          "How much of the extra energy use caused by Speed upgrades the vanilla Energy upgrades cancel (0.0-1.0, default 1.0). At 1.0, equal Speed and Energy upgrades give the base energy per operation; lower values make the line thirstier."),
    GENERAL_EMPOWERED_ENERGY_OFFSET("general.misc.empowered_energy_offset_rate", "Empowered Energy Offset Rate",
          "The same knob for the Empowered Energy line (0.0-1.0, default 0.5). The default cancels only half, so a full Empowered line costs more energy per operation than vanilla; set it to 1.0 for full offset."),
    GENERAL_ENCHANTABLE_MEKA_GEAR("general.misc.enchantable_meka_gear", "Enchantable Meka Gear",
          "Allow Meka Armor/Tool to be enchanted. You need to add the items to the enchantable tags yourself."),
    GENERAL_MAX_OPERATIONS_PER_TICK("general.misc.max_operations_per_tick", "Max Operations Per Tick",
          "Per-tick catch-up cap of the Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator (1-10000, default 128). Speed upgrades past this value have no visible effect on those machines; raising it too far can lag the server."),
    GENERAL_AUTO_SCALE_OPERATIONS("general.misc.auto_scale_operations_per_tick", "Auto-Scale Max Operations Per Tick",
          "When enabled (default), Max Operations Per Tick follows Max Upgrades automatically and is never lowered below your value, so one knob is enough. Disable it to use Max Operations Per Tick as an absolute ceiling."),

    GENERAL_AUTO_INSERTER("general.auto_inserter", "Auto Inserter", "Configurable Auto Inserter", true),
    GENERAL_AUTO_INSERTER_ITEM_RATE("general.auto_inserter.item_rate", "Item Rate", "Configurable item rate for Auto Inserter"),
    GENERAL_AUTO_INSERTER_CHEMICAL_RATE("general.auto_inserter.chemical_rate", "Chemical Rate", "Configurable chemical rate for Auto Inserter"),
    GENERAL_AUTO_INSERTER_FLUID_RATE("general.auto_inserter.fluid_rate", "Fluid Rate", "Configurable fluid rate for Auto Inserter"),
    GENERAL_AUTO_INSERTER_ENERGY_RATE("general.auto_inserter.energy_rate", "Energy Rate", "Configurable energy rate for Auto Inserter"),

    TIER_GAUGE_DROPPER("tiers.gauge_dropper", "Gauge Dropper", "Configurable Gauge Dropper", true),
    TIER_GAUGE_DROPPER_BASIC_RATE("tiers.gauge_dropper.basic_rate", "Basic Rate", "Configurable basic rate for Basic Gauge Dropper"),
    TIER_GAUGE_DROPPER_BASIC_CAPACITY("tiers.gauge_dropper.basic_capacity", "Basic Capacity", "Configurable basic capacity for Basic Gauge Dropper"),
    TIER_GAUGE_DROPPER_ADVANCED_RATE("tiers.gauge_dropper.advanced_rate", "Advanced Rate", "Configurable advanced rate for Advanced Gauge Dropper"),
    TIER_GAUGE_DROPPER_ADVANCED_CAPACITY("tiers.gauge_dropper.advanced_capacity", "Advanced Capacity", "Configurable advanced capacity for Advanced Gauge Dropper"),
    TIER_GAUGE_DROPPER_ELITE_RATE("tiers.gauge_dropper.elite_rate", "Elite Rate", "Configurable elite rate for Elite Gauge Dropper"),
    TIER_GAUGE_DROPPER_ELITE_CAPACITY("tiers.gauge_dropper.elite_capacity", "Elite Capacity", "Configurable elite capacity for Elite Gauge Dropper"),
    TIER_GAUGE_DROPPER_ULTIMATE_RATE("tiers.gauge_dropper.ultimate_rate", "Ultimate Rate", "Configurable ultimate rate for Ultimate Gauge Dropper"),
    TIER_GAUGE_DROPPER_ULTIMATE_CAPACITY("tiers.gauge_dropper.ultimate_capacity", "Ultimate Capacity", "Configurable ultimate capacity for Ultimate Gauge Dropper"),

    ;

    constructor(path: String, title: String, tooltip: String, isSection: Boolean = false) :
        this(path, title, tooltip, IConfigTranslation.getSectionTitle(title, isSection))

    private val key = Util.makeDescriptionId("configuration", MekanismEmpoweredAPI.rl(path))

    override fun title() = title
    override fun tooltip() = tooltip
    override fun button() = button
    override fun getTranslationKey(): String = key
}
