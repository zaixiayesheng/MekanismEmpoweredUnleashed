package dev.lapis256.mekanism_empowered.common.config

import com.electronwill.nightconfig.core.file.CommentedFileConfig
import dev.lapis256.mekanism_empowered.core.common.config.MekanismNestConfig
import net.minecraftforge.common.ForgeConfigSpec
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.fml.config.ConfigFileTypeHandler
import net.minecraftforge.fml.config.ModConfig
import net.minecraftforge.fml.event.config.ModConfigEvent
import net.minecraftforge.fml.event.config.ModConfigEvent.Unloading
import net.minecraftforge.fml.loading.FMLPaths
import java.nio.file.Path
import java.util.function.Function


class MekEmpModConfig(type: Type, spec: ForgeConfigSpec, container: ModContainer, fileName: String, val config: MekanismNestConfig) :
    ModConfig(type, spec, container, fileName) {

    fun clearCache(configEvent: ModConfigEvent) {
        config.clearCache(configEvent is Unloading)
    }

    override fun getHandler() = _handler

    private val _handler = object : ConfigFileTypeHandler() {
        override fun reader(configBasePath: Path): Function<ModConfig, CommentedFileConfig> {
            return super.reader(getPath(configBasePath))
        }

        override fun unload(configBasePath: Path, config: ModConfig) {
            super.unload(getPath(configBasePath), config)
        }

        private fun getPath(configBasePath: Path): Path? {
            if (configBasePath.endsWith("serverconfig")) {
                return FMLPaths.CONFIGDIR.get()
            }
            return configBasePath
        }
    }
}
