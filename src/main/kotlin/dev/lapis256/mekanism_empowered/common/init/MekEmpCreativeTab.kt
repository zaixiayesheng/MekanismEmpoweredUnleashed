package dev.lapis256.mekanism_empowered.common.init

import dev.lapis256.mekanism_empowered.api.MekanismEmpoweredAPI
import mekanism.common.Mekanism
import mekanism.common.registration.impl.CreativeTabDeferredRegister
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.Component
import net.minecraft.world.item.CreativeModeTab
import net.minecraft.world.item.ItemStack
import net.minecraftforge.registries.DeferredRegister


object MekEmpCreativeTab {
    val REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MekanismEmpoweredAPI.MOD_ID)!!

    init {
        REGISTRY.register("main") { ->
            CreativeModeTab.builder()
                // 与 1.21.1 分支一致：走语言键（zh_cn 里是「通用机械：增强·解放」），
                // 原来这里是 Component.literal(MOD_NAME)，创造模式标签页永远显示英文。
                .title(Component.translatable("${MekanismEmpoweredAPI.MOD_ID}.creative_tab"))
                .icon { ItemStack(MekEmpItems.EMPOWERED_SPEED.get()) }
                .displayItems { _, output ->
                     CreativeTabDeferredRegister.addToDisplay(MekEmpItems.REGISTRY, output)
                }
                .withTabsBefore(Mekanism.rl(Mekanism.MODID))
                .build()
        }
    }
}
