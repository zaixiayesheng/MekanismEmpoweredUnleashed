package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import dev.lapis256.mekanism_empowered.core.extension.getInstalled
import mekanism.api.Upgrade
import mekanism.common.config.MekanismConfig
import mekanism.common.tile.base.TileEntityMekanism
import mekanism.common.tile.prefab.TileEntityConfigurableMachine
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


fun TileEntityConfigurableMachine.prcRecalculateAdditionalUpgrades() {
    recalculateUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)
}

/**
 * 大型机器（通用机械：更多机器里的那几台）把「每 tick 操作数」拆成
 * `baseOperations * baselineMaxOperations` 两个 int 相乘，原版 baseline 是 `2^原版速度`。
 * 这个指数一拉高就把每 tick 操作数顶到几百万级：机器每 tick 能领到的操作数远超它的
 * 能量条能支付的数量，于是「实际执行的操作数」永远被能量条卡死成 `容量 / 每操作耗能`，
 * 表现就是每 tick 抽干整条能量条、能耗栏显示一个和容量同量级的离谱数字
 * （用户报的「16 速度 16 能量 16 强化能量，耗能 2.14G FE/t」就是这条路径）。
 *
 * 这里把 baseline 换成和小机器同一口径：`每 tick 操作数 = 倍率^(总速度/8)`，再除以它自己的
 * baseOperations（`4 * max(1, 原版速度)`），让它乘回去的结果正好落在小机器的水平上；
 * 同时对 int 上限留出余量，绝不再溢出。这个换算**始终生效**（不再只在装了强化速度时才作用），
 * 所以插上强化速度时操作数只会单调上升，而不是像 21.1-1.0.8 那样反而被压下去。
 */
fun TileEntityMekanism.modifyRecalculationBaselineMaxOperations(original: Double): Double {
    val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
    val empoweredSpeed = getInstalled(MekEmpUpgrade.EMPOWERED_SPEED) ?: 0

    // 与 MekMM 的 baseOperations 同形：4 * (原版速度 > 0 ? 原版速度 : 原版速度 + 1)
    val baseOperations = 4.0 * max(1, vanillaSpeed)
    val targetOperations = MekanismConfig.general.maxUpgradeMultiplier.get().toDouble()
        .pow((vanillaSpeed + empoweredSpeed) / 8.0)
    // 乘回去时仍然是 int，留一半余量，防止对方的 baseOperations 比预估更大时溢出
    val safeBaseline = Int.MAX_VALUE.toDouble() / baseOperations / 2.0
    // 它自己会把这个值强转成 int，向下取整会让 16 速度只能拿到 64 次操作（目标 100），
    // 所以向上取整，保证插满速度时不会比小机器同口径更慢
    // 下限 1：没有速度升级时也要保持原版那 4 次操作，不能把自己算成 0
    val baseline = max(1.0, min(ceil(targetOperations / baseOperations), safeBaseline))

    if (System.getProperty("mekanism_empowered.debug") != null) {
        MekanismEmpoweredCore.LOGGER.info(
            "[large-machine] speed={} (vanilla {} + empowered {}) baseOperations={} targetPerTick={} baseline={} -> operations={} (native {})",
            vanillaSpeed + empoweredSpeed, vanillaSpeed, empoweredSpeed, baseOperations, targetOperations, baseline,
            baseOperations * baseline.toInt(), original
        )
    }

    return baseline
}
