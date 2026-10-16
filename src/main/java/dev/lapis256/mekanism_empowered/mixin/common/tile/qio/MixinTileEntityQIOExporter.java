package dev.lapis256.mekanism_empowered.mixin.common.tile.qio;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileEntityQIO;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.qio.TileEntityQIOComponent;
import mekanism.common.tile.qio.TileEntityQIOExporter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;


@Mixin(value = TileEntityQIOExporter.class, remap = false)
public class MixinTileEntityQIOExporter extends TileEntityQIOComponent {
    public MixinTileEntityQIOExporter(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    @ModifyExpressionValue(method = "onUpdateServer", at = @At(value = "CONSTANT", args = "intValue=10"))
    protected int mekanismEmpowered$modifyTickDelay(int original) {
        return MixinImplTileEntityQIO.modifyExporterTickDelay(this, original);
    }
}
