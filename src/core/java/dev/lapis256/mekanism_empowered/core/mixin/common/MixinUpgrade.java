package dev.lapis256.mekanism_empowered.core.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.lapis256.mekanism_empowered.core.mixin_impl.MixinImplUpgrade;
import mekanism.api.Upgrade;
import mekanism.api.text.APILang;
import mekanism.api.text.EnumColor;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;


@Mixin(value = Upgrade.class, remap = false, priority = Integer.MAX_VALUE)
public class MixinUpgrade {
    @Shadow
    @Final
    @Mutable
    @Dynamic
    private static Upgrade[] $VALUES;

    @Unique
    private static MixinImplUpgrade mekanismEmpoweredCore$impl;

    @Invoker(value = "<init>")
    public static Upgrade mekanismEmpoweredCore$createUpgradeInstance(String internalName, int internalId, String name, APILang langKey, APILang descLangKey, int maxStack, EnumColor color) {
        throw new AssertionError("Mixin failed to apply, this should never be called");
    }

    @Inject(method = "<clinit>", at = @At(value = "INVOKE", target = "Lmekanism/api/Upgrade;values()[Lmekanism/api/Upgrade;", ordinal = 0))
    private static void mekanismEmpoweredCore$initAdditionalUpgrades(CallbackInfo ci) {
        mekanismEmpoweredCore$impl = new MixinImplUpgrade(MixinUpgrade::mekanismEmpoweredCore$createUpgradeInstance);
        $VALUES = mekanismEmpoweredCore$impl.initAdditionalUpgrades($VALUES);
    }

    @ModifyVariable(method = "buildMap", at = @At(value = "STORE", ordinal = 0), name = "upgrades")
    private static Map<Upgrade, Integer> mekanismEmpoweredCore$buildAdditionalMap(@Nullable Map<Upgrade, Integer> upgrades, @Nullable CompoundTag nbtTags) {
        return mekanismEmpoweredCore$impl.buildAdditionalMap(upgrades, nbtTags);
    }

    /**
     * 把追加升级从原版 {@code upgrades} 列表里摘出来，写进我们自己的标签。
     *
     * 注意 {@code @Local} 只能按类型取（不能写 {@code name = "arg1"}）：
     * 1.21.1 的 Mekanism 里这个方法没有局部变量名，MixinExtras 才会退化成 {@code arg1}；
     * 而 1.20.1 的 Mekanism 10.4 局部变量表里它叫 {@code nbtTags}，
     * 写死 {@code arg1} 会让这个注入静默失效 —— 追加升级就又留在原版列表里，被原版按 maxStack 夹取。
     */
    @ModifyExpressionValue(method = "saveMap", at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;"))
    private static Set<Map.Entry<Upgrade, Integer>> mekanismEmpoweredCore$filterUpgrades(Set<Map.Entry<Upgrade, Integer>> original, @Local(argsOnly = true) CompoundTag nbtTags) {
        return mekanismEmpoweredCore$impl.saveAdditionalMap(original, nbtTags);
    }
}
