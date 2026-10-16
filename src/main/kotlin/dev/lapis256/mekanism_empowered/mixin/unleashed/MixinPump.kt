package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.unleashed.RawTicksHolder
import dev.lapis256.mekanism_empowered.unleashed.Temp
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.common.tile.machine.TileEntityElectricPump
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Shadow
import org.spongepowered.asm.mixin.Unique
import org.spongepowered.asm.mixin.injection.At
import org.spongepowered.asm.mixin.injection.Inject
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo
import kotlin.math.max
import kotlin.math.min

/**
 * 电动泵：支持"每 tick 多次抽取"。
 *
 * 每次完成一次抽取（`suck`）之后，按 [RawTicksHolder] 暂存的原始编码值
 * 换算本 tick 还应补跑的次数，通过 [Temp.inject] 循环补跑 onUpdateServer。
 * 补跑的每一次都会重新走完整的原版 tick（含能量扣除），所以补抽同样耗电。
 * 递归由 [Temp.isInjecting] 拦截。
 *
 * 与无用之物（UselessMod）并存：它同点位的补次回调读的是
 * `ticksRequired` 字段——该字段恒 >= 1（负值只存在于我们的暂存值里），
 * 所以它的补次循环天然空转，这里无需任何 mod id 判断。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [TileEntityElectricPump::class], remap = false)
abstract class MixinPump : RawTicksHolder {

    /** 原版 getTicks 钳制前的原始编码值（负 = 一 tick 多次抽取）。 */
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
                target = "Lmekanism/common/tile/machine/TileEntityElectricPump;suck()Z",
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
