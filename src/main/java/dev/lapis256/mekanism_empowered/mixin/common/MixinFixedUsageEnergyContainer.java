package dev.lapis256.mekanism_empowered.mixin.common;

import dev.lapis256.mekanism_empowered.mixin_impl.MachineEnergyContainerAccessor;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplMachineEnergyContainer;
import mekanism.common.capabilities.energy.FixedUsageEnergyContainer;
import mekanism.common.tile.base.TileEntityMekanism;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * FixedUsageEnergyContainer 自己重写了 updateEnergyPerTick，把 baseEnergyCalculator 的结果直接塞进
 * currentEnergyPerTick，既不经过 MekanismUtils.getEnergyPerTick（我们的能耗缩放对它无效），
 * 也绕过了祖先那一层的钳制。大型电解分离机用的就是它，这里补上同一道「能耗不超过储能」的保险，
 * 并把大型机器的能耗补偿补上。
 */
@Mixin(value = FixedUsageEnergyContainer.class, remap = false)
public abstract class MixinFixedUsageEnergyContainer {
    @Inject(method = "updateEnergyPerTick", at = @At("RETURN"))
    private void mekanismEmpowered$clampEnergyPerTick(CallbackInfo ci) {
        FixedUsageEnergyContainer<?> self = (FixedUsageEnergyContainer<?>) (Object) this;
        MixinImplMachineEnergyContainer.clampEnergyPerTick(self);
        MixinImplMachineEnergyContainer.applyFixedUsageCompensation(self, ((MachineEnergyContainerAccessor) (Object) this).mekanismEmpowered$getTile());
    }
}
