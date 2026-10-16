package dev.lapis256.mekanism_empowered.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplMekanismUtils;
import mekanism.api.Upgrade;
import mekanism.api.math.FloatingLong;
import mekanism.common.tile.interfaces.IUpgradeTile;
import mekanism.common.util.MekanismUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.Map;


@Mixin(value = MekanismUtils.class, remap = false)
public class MixinMekanismUtils {
    @ModifyArg(method = "getTicks", at = @At(value = "INVOKE", target = "Lmekanism/api/math/MathUtils;clampToInt(D)I", ordinal = 0))
    private static double mekanismEmpowered$modifyTicks(double original, @Local(argsOnly = true) IUpgradeTile tile) {
        return MixinImplMekanismUtils.modifyTicks(tile, original);
    }

    @ModifyReturnValue(method = "getEnergyPerTick", at = @At(value = "RETURN", ordinal = 0))
    private static FloatingLong mekanismEmpowered$modifyEnergyPerTick(FloatingLong original, @Local(argsOnly = true) IUpgradeTile tile) {
        return MixinImplMekanismUtils.modifyEnergyPerTick(tile, original);
    }

    @ModifyReturnValue(method = "getMaxEnergy(Lmekanism/common/tile/interfaces/IUpgradeTile;Lmekanism/api/math/FloatingLong;)Lmekanism/api/math/FloatingLong;", at = @At(value = "RETURN", ordinal = 0))
    private static FloatingLong mekanismEmpowered$modifyTileMaxEnergy(FloatingLong original, @Local(argsOnly = true) IUpgradeTile tile) {
        return MixinImplMekanismUtils.modifyTileMaxEnergy(tile, original);
    }

    @ModifyExpressionValue(method = "getMaxEnergy(Lnet/minecraft/world/item/ItemStack;Lmekanism/api/math/FloatingLong;)Lmekanism/api/math/FloatingLong;", at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;buildMap(Lnet/minecraft/nbt/CompoundTag;)Ljava/util/Map;"))
    private static Map<Upgrade, Integer> mekanismEmpowered$modifyItemStackMaxEnergyStoreUpgrades(Map<Upgrade, Integer> original, @Share("upgrades") LocalRef<Map<Upgrade, Integer>> upgrades) {
        upgrades.set(original);
        return original;
    }

    @ModifyReturnValue(method = "getMaxEnergy(Lnet/minecraft/world/item/ItemStack;Lmekanism/api/math/FloatingLong;)Lmekanism/api/math/FloatingLong;", at = @At(value = "RETURN"))
    private static FloatingLong mekanismEmpowered$modifyItemStackMaxEnergy(FloatingLong original, @Local float numUpgrades, @Share("upgrades") LocalRef<Map<Upgrade, Integer>> upgrades) {
        return MixinImplMekanismUtils.modifyItemStackMaxEnergy(original, numUpgrades, upgrades.get());
    }
}
