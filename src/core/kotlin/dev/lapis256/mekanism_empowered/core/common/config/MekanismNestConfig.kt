package dev.lapis256.mekanism_empowered.core.common.config

import dev.lapis256.easy_nest_config.api.AbstractNestConfig
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import mekanism.common.config.IMekanismConfig
import mekanism.common.config.value.*
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.config.ModConfig
import kotlin.reflect.KProperty


@Suppress("Unused")
abstract class MekanismNestConfig(type: ModConfig.Type, name: String) : AbstractNestConfig(type, name), IMekanismConfig {
    private val cachedConfigValues: MutableList<CachedValue<*>> = mutableListOf()

    /**
     * 同步保存：调用方（Mekanism 的配置界面 / 退出流程）按「[save] 返回即已落盘」行事。
     * 原来的 `ConfigSaveScope.launch` 版本里，游戏内改完配置立刻退出时 IO 协程可能被掐断，
     * 改动静默丢失。保留原来的 3 次重试语义。
     */
    override fun save() {
        for (attempt in 0..2) {
            val result = runCatching { configSpec.save() }
            if (result.isSuccess) return
            MekanismEmpoweredCore.LOGGER.error("Failed to save config", result.exceptionOrNull())
            if (attempt == 2) MekanismEmpoweredCore.LOGGER.error("Giving up")
        }
    }

    override fun clearCache(unloading: Boolean) {
        cachedConfigValues.forEach { it.clearCache(unloading) }
    }

    override fun addCachedValue(configValue: CachedValue<*>) {
        cachedConfigValues.add(configValue)
    }

    override fun getFileName(): String = name
    override fun getConfigType(): ModConfig.Type = type
    override fun getConfigSpec(): ForgeConfigSpec = spec


    fun ForgeConfigSpec.ConfigValue<Boolean>.cached(): CachedBooleanValue = CachedBooleanValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Byte>.cached(): CachedByteValue = CachedByteValue.wrap(this@MekanismNestConfig, this)
    fun <T> ForgeConfigSpec.ConfigValue<T>.cached(): CachedConfigValue<T> = CachedConfigValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Double>.cached(): CachedDoubleValue = CachedDoubleValue.wrap(this@MekanismNestConfig, this)
    fun <E : Enum<E>> ForgeConfigSpec.EnumValue<E>.cached(): CachedConfigValue<E> = CachedEnumValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Double>.cachedF(): CachedFloatValue = CachedFloatValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Int>.cached(): CachedIntValue = CachedIntValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Long>.cached(): CachedLongValue = CachedLongValue.wrap(this@MekanismNestConfig, this)
    fun ForgeConfigSpec.ConfigValue<Short>.cached(): CachedShortValue = CachedShortValue.wrap(this@MekanismNestConfig, this)

    operator fun CachedBooleanValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedByteValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun <T> CachedConfigValue<T>.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedDoubleValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun <E : Enum<E>> CachedEnumValue<E>.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedFloatValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedIntValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedLongValue.getValue(a: Any?, p: KProperty<*>) = get()
    operator fun CachedShortValue.getValue(a: Any?, p: KProperty<*>) = get()
}
