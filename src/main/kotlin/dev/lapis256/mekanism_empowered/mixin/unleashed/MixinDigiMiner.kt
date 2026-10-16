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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import kotlin.math.max
import kotlin.math.min

/**
 * 数字矿机：支持"每 tick 多次采矿"。
 *
 * 注入点在每 tick 的能量扣除之后（`MinerEnergyContainer.extract` 的
 * 第二次调用 = 真正挖矿那一次），按 [RawTicksHolder] 暂存的原始编码值
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
 * 1.20.1 适配：能量容器的 `extract` 在 1.20.1 收/发的是
 * `mekanism.api.math.FloatingLong`（1.21.1 是 long），所以目标描述符
 * 必须跟着换；序号 1 仍然指"真正执行的那一次"（序号 0 是 SIMULATE 预演）。
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

    /** 原版每 tick 更新入口（1.20.1 是 void，1.21.1 起才有返回值）。 */
    @Shadow
    protected abstract fun onUpdateServer()

    @Inject(
        method = ["onUpdateServer"],
        at = [
            At(
                value = "INVOKE",
                target = "Lmekanism/common/capabilities/energy/MinerEnergyContainer;extract(Lmekanism/api/math/FloatingLong;Lmekanism/api/Action;Lmekanism/api/AutomationType;)Lmekanism/api/math/FloatingLong;",
                ordinal = 1,
                shift = At.Shift.AFTER
            )
        ]
    )
    fun injected(ci: CallbackInfo) {
        // 本 tick 总操作次数 = -原始编码值；原版流程本身已执行 1 次，
        // 这里补齐剩余次数，上限由配置 maxOperationsPerTick 决定（默认 128）。
        val extras = min(UnleashedUtils.maxOperationsPerTick(), max(1, -rawTicks) - 1)
        if (extras > 0) {
            Temp.inject.accept(-extras, Runnable { onUpdateServer() })
        }
    }
}
