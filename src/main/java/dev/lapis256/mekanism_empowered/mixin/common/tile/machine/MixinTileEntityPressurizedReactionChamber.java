package dev.lapis256.mekanism_empowered.mixin.common.tile.machine;

import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileMachineKt;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityPressurizedReactionChamber;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * 该 mixin 同时覆盖原版加压反应室与 mekaf 的附属工厂（targets 里的第三方类），且带 @Pseudo。
 * <p>
 * 【健壮性】本模组的 mixin 配置里 {@code injectors.defaultRequire = 1}
 * （见 mekanism_empowered_unleashed.mixins.json），附属模组的 onCachedRecipeChanged 里
 * 只要没有这处 {@code Upgrade.SPEED} 的 GETSTATIC 模式，启动期就会抛注入错误直接崩游戏；
 * @Pseudo 无法豁免注入失败。因此这里显式写 require = 0（1.21.1 合并版是把 defaultRequire
 * 全局降为 0 达到同样效果的，见 AUDIT-2026-10-16.md 的 A17）。
 * <p>
 * 代价是同一条 @Inject 对原版类也放宽了：原版方法体必然匹配本模组编译所依赖的 Mekanism
 * 版本，实际影响可以忽略；若要严格，可把原版类与第三方类拆成两个 mixin 类（本次未做）。
 */
@Pseudo
@Mixin(
    value = TileEntityPressurizedReactionChamber.class,
    targets = {
        "com.jerry.mekaf.common.tile.TileEntityPressurizedReactingFactory"
    },
    remap = false
)
public abstract class MixinTileEntityPressurizedReactionChamber extends TileEntityMekanism {
    public MixinTileEntityPressurizedReactionChamber(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Inject(method = "onCachedRecipeChanged", at = @At(value = "FIELD", target = "Lmekanism/api/Upgrade;SPEED:Lmekanism/api/Upgrade;", opcode = Opcodes.GETSTATIC), require = 0)
    private void mekanismEmpowered$recalculateAdditionalUpgrades(CallbackInfo ci) {
        MixinImplTileMachineKt.prcRecalculateAdditionalUpgrades(this);
    }
}
