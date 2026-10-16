package dev.lapis256.mekanism_empowered.data.provider

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.api.text.MekEmpAPILang
import dev.lapis256.mekanism_empowered.common.MekEmpLang
import dev.lapis256.mekanism_empowered.common.init.MekEmpItems
import dev.lapis256.mekanism_empowered.core.api.text.ILangEnglishHolder
import mekanism.api.text.ILangEntry
import net.minecraft.data.PackOutput
import net.minecraftforge.common.data.LanguageProvider
import kotlin.reflect.KClass


class MekEmpLanguageProvider(output: PackOutput) : LanguageProvider(output, MekanismEmpoweredAPI.MOD_ID, "en_us") {
    override fun addTranslations() {
        addLangEnum(MekEmpAPILang::class)
        addLangEnum(MekEmpLang::class)

        addItem(MekEmpItems.EMPOWERED_SPEED, "Empowered Speed Upgrade")
        addItem(MekEmpItems.EMPOWERED_ENERGY, "Empowered Energy Upgrade")
        addItem(MekEmpItems.FAST_ITEM_EJECT, "Fast Item Eject Upgrade")
        addItem(MekEmpItems.FAST_ITEM_INSERT, "Fast Item Insert Upgrade")
        addItem(MekEmpItems.AUTO_INSERTER, "Auto Insert Upgrade")
        addItem(MekEmpItems.IO_CAPACITY, "I/O Capacity Upgrade")

        addItem(MekEmpItems.BASIC_GAUGE_DROPPER, "Basic Gauge Dropper")
        addItem(MekEmpItems.ADVANCED_GAUGE_DROPPER, "Advanced Gauge Dropper")
        addItem(MekEmpItems.ELITE_GAUGE_DROPPER, "Elite Gauge Dropper")
        addItem(MekEmpItems.ULTIMATE_GAUGE_DROPPER, "Ultimate Gauge Dropper")
    }

    fun <E> addLangEnum(enum: KClass<out E>) where E : Enum<E>, E : ILangEntry, E : ILangEnglishHolder {
        for (entry in enum.java.enumConstants) {
            add(entry.translationKey, entry.english)
        }
    }
}
