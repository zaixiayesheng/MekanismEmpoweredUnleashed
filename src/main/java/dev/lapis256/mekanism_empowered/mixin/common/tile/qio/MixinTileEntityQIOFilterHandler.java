package dev.lapis256.mekanism_empowered.mixin.common.tile.qio;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileEntityQIO;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIOFilterHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(value = TileEntityQIOFilterHandler.class, remap = false)
public abstract class MixinTileEntityQIOFilterHandler extends TileEntityQIOComponent {
    public MixinTileEntityQIOFilterHandler(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @Definition(id = "speedUpgrades", local = @Local(type = int.class, ordinal = 0))
    @Expression("? + ? * speedUpgrades")
    @ModifyReturnValue(method = "getMaxTransitCount", at = @At("RETURN"))
    protected int mekanismEmpowered$modifyMaxTransitCount(int original) {
        return MixinImplTileEntityQIO.modifyMaxTransitCount(this, original);
    }

    @ModifyReturnValue(method = "getMaxTransitTypes", at = @At(value = "RETURN"))
    protected int mekanismEmpowered$modifyMaxTransitTypes(int original) {
        return MixinImplTileEntityQIO.modifyMaxTransitTypes(this, original);
    }
}
