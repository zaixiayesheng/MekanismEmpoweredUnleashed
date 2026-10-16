package dev.lapis256.mekanism_empowered.mixin.common.tile;

import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileEntityMekanism;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.component.TileComponentUpgrade;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(value = TileEntityMekanism.class, remap = false)
public class MixinTileEntityMekanism {
    @Shadow
    @Final
    protected IBlockProvider blockProvider;

    @Shadow
    private boolean supportsUpgrades;

    @Shadow
    protected TileComponentUpgrade upgradeComponent;

    /**
     * Fallback for block types not already patched through AdditionalUpgradeUtil.addSupported.
     */
    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void mekanismEmpowered$init(CallbackInfo ci) {
        var self = (TileEntityMekanism) (Object) this;
        var isModified = MixinImplTileEntityMekanism.applyFallbackSupportedUpgrades(self, blockProvider.getBlock());
        if (isModified && supportsUpgrades) {
            upgradeComponent = new TileComponentUpgrade(self);
        }
    }
}
