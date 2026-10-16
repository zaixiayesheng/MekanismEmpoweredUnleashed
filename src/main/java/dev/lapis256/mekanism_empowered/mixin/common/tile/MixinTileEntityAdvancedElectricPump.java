package dev.lapis256.mekanism_empowered.mixin.common.tile;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileEntityElectricPump;
import mekanism.common.tile.interfaces.IUpgradeTile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;


/**
 * 扩展泵（mekanism_extras）的三处注入：目标全是第三方类，且本类带 @Pseudo。
 * <p>
 * 【健壮性】本模组的 mixin 配置里 {@code injectors.defaultRequire = 1}
 * （见 mekanism_empowered_unleashed.mixins.json），只要装了扩展模组、而它的
 * 字节码与这里的模式对不上（附属模组换版本、换了实现），启动期就会抛注入错误
 * 直接崩游戏；@Pseudo 无法豁免注入失败。所以这三处各写 require = 0：
 * 模式不存在的目标只是跳过这处改写（泵仍走原版逻辑，只是没有强化加成），
 * 不再因为附属模组的版本差异而启动失败。1.21.1 合并版是把 defaultRequire
 * 全局降为 0 来达到同样效果的（见 AUDIT-2026-10-16.md 的 A17）。
 */
@Pseudo
@Mixin(targets = "com.jerry.mekanism_extras.common.tile.machine.TileEntityAdvancedElectricPump", remap = false)
public abstract class MixinTileEntityAdvancedElectricPump implements IUpgradeTile {

    @ModifyArg(method = "onUpdateServer", at = @At(value = "INVOKE", target = "Lmekanism/common/util/FluidUtils;emit(Ljava/util/Set;Lmekanism/api/fluid/IExtendedFluidTank;Lnet/minecraft/world/level/block/entity/BlockEntity;I)V"), require = 0)
    private int mekanismEmpowered$modifyOutputRate(int original) {
        return MixinImplTileEntityElectricPump.modifyOutputRate(this, original);
    }

    @ModifyArg(method = "getOutput", at = @At(value = "INVOKE", target = "Lmekanism/common/registration/impl/FluidRegistryObject;getFluidStack(I)Lnet/minecraftforge/fluids/FluidStack;"), require = 0)
    private int mekanismEmpowered$modifyHeavyWaterOutputAmount(int original) {
        return MixinImplTileEntityElectricPump.modifyWaterOutputAmount(this, original);
    }

    @ModifyExpressionValue(method = "getOutput", at = @At(value = "CONSTANT", args = "intValue=100000"), require = 0)
    private int mekanismEmpowered$modifyWaterOutputAmount(int original) {
        return MixinImplTileEntityElectricPump.modifyWaterOutputAmount(this, original);
    }
}
