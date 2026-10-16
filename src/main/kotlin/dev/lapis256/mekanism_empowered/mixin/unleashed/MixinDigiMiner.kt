package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.unleashed.RawTicksHolder
import dev.lapis256.mekanism_empowered.unleashed.Temp
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.common.tile.machine.TileEntityDigitalMiner
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable
import kotlin.math.max
import kotlin.math.min

/**
 * 数字矿机：支持"每 tick 多次采矿"。
 *
 * 注入点在每 tick 的能量扣除之后，按 [RawTicksHolder] 暂存的原始编码值
 * 经 [Temp.inject] 补跑 onUpdateServer：补的每一次都要重新走完整的原版
 * tick（含能量检查与原版延迟），所以补采同样耗电，不存在白挖矿。
 * 递归由 [Temp.isInjecting] 拦截。
 *
 * 机制与电动泵/流体装填器/公式装配器完全一致。
 *
 * 与无用之物（UselessMod）并存：它同点位的补次回调读的是
 * `delay` 字段——该字段恒 >= 1（负值只存在于我们的暂存值里），
 * 所以它的补次循环天然空转，这里无需任何 mod id 判断。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [TileEntityDigitalMiner::class], remap = false)
abstract class MixinDigiMiner : RawTicksHolder {

    /** 原版 getTicks 钳制前的原始编码值（负 = 一 tick 多次采矿）。 */
    @field:Unique
    @JvmField
    var rawTicks: Int = 1

    override fun getRawTicks(): Int = rawTicks

    override fun setRawTicks(raw: Int) {
        rawTicks = raw
    }

    /** 原版每 tick 更新入口。 */
    @Shadow
    protected abstract fun onUpdateServer(): Boolean

    @Inject(
        method = ["onUpdateServer"],
        at = [
            At(
                value = "INVOKE",
                target = "Lmekanism/common/capabilities/energy/MinerEnergyContainer;extract(JLmekanism/api/Action;Lmekanism/api/AutomationType;)J",
                ordinal = 1,
                shift = At.Shift.AFTER
            )
        ]
    )
    fun injected(cir: CallbackInfoReturnable<Boolean>) {
        // 本 tick 总操作次数 = -原始编码值；原版流程本身已执行 1 次，
        // 这里补齐剩余次数，上限由配置 maxOperationsPerTick 决定（默认 128）。
        val extras = min(UnleashedUtils.maxOperationsPerTick(), max(1, -rawTicks) - 1)
        if (extras > 0) {
            Temp.inject.accept(-extras, Runnable { onUpdateServer() })
        }
    }
}
