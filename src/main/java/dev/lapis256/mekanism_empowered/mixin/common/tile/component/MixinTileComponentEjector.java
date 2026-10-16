package dev.lapis256.mekanism_empowered.mixin.common.tile.component;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileComponentEjector;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentEjector;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


/**
 * 弹出速率与容量。
 * <p>
 * 优先级 2000：无用之物（UselessMod）也在 {@code outputItems} 的同一个返回点上
 * 改写 {@code tickDelay}，同一注入点按「优先级升序应用」，数值越大越晚执行，
 * 2000 才能保证最后说话的是我们。1.21.1 分支上这个优先级是必须项，
 * 缺了会出现"装了无用之物以后弹出速率不对"。
 */
@Mixin(value = TileComponentEjector.class, remap = false, priority = 2000)
public class MixinTileComponentEjector {
    @Shadow
    @Final
    private TileEntityMekanism tile;

    @ModifyExpressionValue(method = "outputItems", at = @At(value = "CONSTANT", args = "intValue=10"))
    private int mekanismEmpowered$modifyTickDelay(int original) {
        return MixinImplTileComponentEjector.modifyTickDelay(tile, original);
    }

    @ModifyArg(method = "eject", at = @At(value = "INVOKE", target = "Lmekanism/common/util/FluidUtils;emit(Ljava/util/Set;Lmekanism/api/fluid/IExtendedFluidTank;Lnet/minecraft/world/level/block/entity/BlockEntity;I)V"), index = 3)
    private int mekanismEmpowered$modifyFluidEjectRate(int original) {
        return MixinImplTileComponentEjector.modifyFluidEjectRate(tile, original);
    }

    @ModifyArg(method = "eject", at = @At(value = "INVOKE", target = "Lmekanism/common/util/ChemicalUtil;emit(Ljava/util/Set;Lmekanism/api/chemical/IChemicalTank;Lnet/minecraft/world/level/block/entity/BlockEntity;J)V"), index = 3)
    private long mekanismEmpowered$modifyChemicalEjectRate(long original) {
        return MixinImplTileComponentEjector.modifyChemicalEjectRate(tile, original);
    }
}
