package dev.lapis256.mekanism_empowered.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lapis256.mekanism_empowered.common.item.ItemTieredGaugeDropper;
import mekanism.common.network.to_server.PacketDropperUse;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;


/**
 * 让带容量的量滴管（{@link ItemTieredGaugeDropper}）走「填充」分支。
 *
 * 客户端算出来的动作是 DUMP_TANK（倾倒），但我们的分级量滴管没有「倾倒」语义，
 * 只会在服务端被当成空操作。这里在服务端把动作改成 DRAIN/FILL 对应的 FILL_DROPPER，
 * 与 1.21.1 分支的实现一致（1.20.1 的 PacketDropperUse 结构相同，无需改动）。
 */
@Mixin(value = PacketDropperUse.class, remap = false)
public class MixinPacketDropperUse {
    @Shadow
    @Final
    @Mutable
    private PacketDropperUse.DropperAction action;

    @ModifyExpressionValue(method = "handle", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;"))
    private Item onHandle(Item original) {
        if (action == PacketDropperUse.DropperAction.DUMP_TANK && original instanceof ItemTieredGaugeDropper) {
            action = PacketDropperUse.DropperAction.FILL_DROPPER;
        }

        return original;
    }
}
