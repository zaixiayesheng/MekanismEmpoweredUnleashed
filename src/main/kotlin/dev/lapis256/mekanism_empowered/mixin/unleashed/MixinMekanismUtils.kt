package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.core.extension.times
import dev.lapis256.mekanism_empowered.mixin_impl.MixinImplMekanismUtils
import dev.lapis256.mekanism_empowered.unleashed.RawTicksHolder
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.math.FloatingLong
import mekanism.common.tile.interfaces.IUpgradeTile
import mekanism.api.math.MathUtils
import mekanism.common.util.MekanismUtils
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * 原版三大公式的"绝对式"替换：升级上限抬高之后，每个原版速度/能量升级
 * 仍然按原版力度生效（耗时 × 倍率^(-速度数/8)、能耗 × 倍率^指数、
 * 容量 × 倍率^(能量数/8)），而不是像原版那样把倍率平摊到当前上限上。
 *
 * 为什么在 1.20.1 上要用 HEAD 注入整体接管、而不是像 1.21.1 那样
 * 只改一个中间值：
 *
 * - 1.21.1 的 `MekanismUtils.getTicksD` 返回 double，Unleashed 侧只要
 *   乘一个系数就行；
 * - 1.20.1 的 `MekanismUtils.getTicks` 内部已经把
 *   `def * 倍率^(-原版占比)` 算完并 `clampToInt` 成 int，倍率是
 *   **除以当前上限**的占比式，没有可插入的绝对式入口。
 *
 * 接管时仍然复用 Empowered 的第二层（强化升级）缩放
 * [MixinImplMekanismUtils]，所以"原版插满 → 强化升级再叠一层"的行为
 * 与 1.21.1 一致；被我们取消的原版方法体里的 Empowered 注入点
 * （@ModifyArg / @ModifyReturnValue）不会再触发，不会重复计算。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [MekanismUtils::class], remap = false, priority = 500)
class MixinMekanismUtils {

    /*
     * 注意：Kotlin 的 companion object 会生成非私有的静态字段，
     * 违反 Mixin 的硬规则，所以必须声明为 private companion。
     */
    private companion object {

        /** 单次操作耗时：绝对式速度系数 × 原版上下限钳制。 */
        @JvmStatic
        @Inject(method = ["getTicks"], at = [At("HEAD")], cancellable = true)
        private fun mekanismUnleashedGetTicks(
            tile: IUpgradeTile,
            def: Int,
            cir: CallbackInfoReturnable<Int>
        ) {
            if (!tile.supportsUpgrades()) {
                cir.setReturnValue(def)
                return
            }

            val base = def * UnleashedUtils.time(tile)
            // 接 Empowered 的第二层缩放（仅在原版速度线插满时生效）。
            val scaled = MixinImplMekanismUtils.run { tile.modifyTicks(base) }
            // 至少 1 tick：0 会让机器的 tick 计数与取模运算失去意义。
            val ticks = max(1, MathUtils.clampToInt(scaled))
            // 原版会把结果钳到 >= 1，"一 tick 多次操作"没法通过返回值传递。
            // 于是仿照 1.21.1 的做法：在钳制前后把编码值暂存到方块实体上
            // （负值 = 本 tick 应执行的操作次数），四台特殊机器的补次回调
            // 再把它换算成补跑次数；普通机器不实现该接口，这里只是一次类型判断。
            (tile as? RawTicksHolder)?.setRawTicks(
                if (scaled < 1.0) -operationsPerTick(def, scaled) else ticks
            )
            cir.setReturnValue(ticks)
        }

        /**
         * "一 tick 多次操作"模式下的操作次数：基础间隔 ÷ 实际间隔，
         * 至少 2 次（1 次不需要补跑），上限由配置 maxOperationsPerTick 决定。
         */
        private fun operationsPerTick(def: Int, scaled: Double): Int {
            if (scaled <= 0.0) return UnleashedUtils.maxOperationsPerTick()
            return min(UnleashedUtils.maxOperationsPerTick(), max(2, ceil(def / scaled).toInt()))
        }

        /** 每 tick 能耗：绝对式能量指数 × 基础值。 */
        @JvmStatic
        @Inject(method = ["getEnergyPerTick"], at = [At("HEAD")], cancellable = true)
        private fun mekanismUnleashedGetEnergyPerTick(
            tile: IUpgradeTile,
            def: FloatingLong,
            cir: CallbackInfoReturnable<FloatingLong>
        ) {
            val base = def * UnleashedUtils.energy(tile)
            val tier2 = MixinImplMekanismUtils.run { tile.modifyEnergyPerTick(base) }
            cir.setReturnValue(MixinImplMekanismUtils.run { tile.applyLargeMachineEnergyCompensation(tier2) })
        }

        /** 能量容量：绝对式容量系数 × 基础值（描述符区分方块侧与物品侧重载）。 */
        @JvmStatic
        @Inject(
            method = ["getMaxEnergy(Lmekanism/common/tile/interfaces/IUpgradeTile;Lmekanism/api/math/FloatingLong;)Lmekanism/api/math/FloatingLong;"],
            at = [At("HEAD")],
            cancellable = true
        )
        private fun mekanismUnleashedGetMaxEnergy(
            tile: IUpgradeTile,
            def: FloatingLong,
            cir: CallbackInfoReturnable<FloatingLong>
        ) {
            val base = def * UnleashedUtils.capacity(tile)
            cir.setReturnValue(MixinImplMekanismUtils.run { tile.modifyTileMaxEnergy(base) })
        }
    }
}
