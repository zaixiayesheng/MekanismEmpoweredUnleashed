package dev.lapis256.mekanism_empowered.mixin.common.tile.machine;


import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplTileMachineKt;
import mekanism.api.providers.IBlockProvider;
import mekanism.common.tile.base.TileEntityMekanism;
import mekanism.common.tile.machine.TileEntityChemicalInfuser;
import mekanism.common.tile.machine.TileEntityChemicalWasher;
import mekanism.common.tile.machine.TileEntityElectrolyticSeparator;
import mekanism.common.tile.machine.TileEntityIsotopicCentrifuge;
import mekanism.common.tile.machine.TileEntityPigmentMixer;
import mekanism.common.tile.machine.TileEntityRotaryCondensentrator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;


@Pseudo
@Mixin(
    value = {
        TileEntityChemicalInfuser.class,
        TileEntityChemicalWasher.class,
        TileEntityElectrolyticSeparator.class,
        TileEntityIsotopicCentrifuge.class,
        TileEntityPigmentMixer.class,
        TileEntityRotaryCondensentrator.class,
    },
    targets = {
        // 注意：mekaf 的 TileEntityAdvancedFactoryBase.recalculateUpgrades 里没有 Math.pow
        // （只有 getTicks/getOperationsPerTick），放这里会因为匹配不到而注入失败；
        // 它的速度重算由 MixinModifyRecalculationTarget$Speed 负责。（结论同 1.21.1 合并版）
        "com.jerry.meklm.common.tile.machine.TileEntityLargeChemicalInfuser",
        "com.jerry.meklm.common.tile.machine.TileEntityLargeElectrolyticSeparator",
        "com.jerry.meklm.common.tile.machine.TileEntityLargePigmentMixer",
        "com.jerry.meklm.common.tile.machine.TileEntityLargeRotaryCondensentrator",
        "com.jerry.meklm.common.tile.machine.TileEntityLargeSolarNeutronActivator",
    },
    remap = false
)
public abstract class MixinModifyRecalculationBaselineMaxOperations extends TileEntityMekanism {
    public MixinModifyRecalculationBaselineMaxOperations(IBlockProvider blockProvider, BlockPos pos, BlockState state) {
        super(blockProvider, pos, state);
    }

    /**
     * ordinal = 0 是必须的：本 mixin 覆盖多个类，某个目标的 recalculateUpgrades 里出现
     * 第二个 Math.pow 时，不写 ordinal 就会改错地方。
     * <p>
     * require = 0 的理由同本文件 targets 的注释：附属机器的 recalculateUpgrades 实现各不
     * 相同，一旦对不上，默认的 require = 1 会在启动期直接崩游戏；显式写 0 后只是跳过这处改写。
     */
    @ModifyExpressionValue(method = "recalculateUpgrades", at = @At(value = "INVOKE", target = "Ljava/lang/Math;pow(DD)D", ordinal = 0), require = 0)
    private double mekanismEmpowered$modifyRecalculationBaselineMaxOperations(double original) {
        return MixinImplTileMachineKt.modifyRecalculationBaselineMaxOperations(this, original);
    }
}
