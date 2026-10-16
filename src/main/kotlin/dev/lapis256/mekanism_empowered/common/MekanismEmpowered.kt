package dev.lapis256.mekanism_empowered.common

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import dev.lapis256.mekanism_empowered.client.MekanismEmpoweredClient
import dev.lapis256.mekanism_empowered.common.config.MekEmpConfig
import dev.lapis256.mekanism_empowered.common.init.MekEmpCreativeTab
import dev.lapis256.mekanism_empowered.common.init.MekEmpItems
import dev.lapis256.mekanism_empowered.common.init.MekEmpUpgrades
import dev.lapis256.mekanism_empowered.common.network.MekEmpPacketHandler
import dev.lapis256.mekanism_empowered.core.common.init.GlobalLootModifierSerializers
import dev.lapis256.mekanism_empowered.core.common.init.LootConditionTypes
import dev.lapis256.mekanism_empowered.core.common.util.AdditionalUpgradeUtil
import dev.lapis256.mekanism_empowered.integration.Integrations
import mekanism.common.lib.Version
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory


@Mod(MekanismEmpoweredAPI.MOD_ID)
class MekanismEmpowered(context: FMLJavaModLoadingContext) {
    init {
        MekEmpConfig.registerConfigs(context)

        MekEmpUpgrades.registerSupportedUpgrades()
        MekEmpUpgrades.registerUpgradeInfo()

        val modEventBus = context.modEventBus
        modEventBus.addListener(MekEmpConfig::onConfigLoad)

        MekEmpItems.REGISTRY.register(modEventBus)
        MekEmpCreativeTab.REGISTRY.register(modEventBus)

        // === 以下三行合并自 Mekanism: Empowered Core（原本是独立 @Mod 入口的初始化） ===
        // 战利品修饰器序列化器 + 战利品条件类型注册（原 core 的注册内容）
        GlobalLootModifierSerializers.REGISTRY.register(modEventBus)
        LootConditionTypes.REGISTRY.register(modEventBus)
        // 公共设置阶段：应用各机器延迟注册的"支持升级"清单
        modEventBus.addListener<FMLCommonSetupEvent> {
            AdditionalUpgradeUtil.applyDeferredSupportedUpgrades()
        }

        Integrations.initCommon(modEventBus)

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT) {
            Runnable {
                MekanismEmpoweredClient.init(modEventBus)
            }
        }

        instance = this
    }

    val versionNumber = Version(context.container)
    val packetHandler = MekEmpPacketHandler(versionNumber).apply { initialize() }

    companion object {
        @JvmField
        val LOGGER: Logger = LoggerFactory.getLogger(MekanismEmpoweredAPI.MOD_ID)

        lateinit var instance: MekanismEmpowered
            private set

        val packetHandler get() = instance.packetHandler
    }
}
