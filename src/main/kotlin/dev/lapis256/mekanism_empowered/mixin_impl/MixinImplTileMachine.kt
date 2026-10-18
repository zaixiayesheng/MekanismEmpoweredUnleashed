package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import dev.lapis256.mekanism_empowered.core.extension.getInstalled
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import mekanism.api.Upgrade
import mekanism.common.tile.base.TileEntityMekanism
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


fun TileEntityMekanism.prcRecalculateAdditionalUpgrades() {
    recalculateUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)
}

/**
 * 大型机器（通用机械：更多机器里的那几台）的「每 tick 操作数」是
 * `baseOperations * baselineMaxOperations` 两个 int 相乘，原版 baseline 就是 `2^原版速度`。
 *
 * 这个函数只做两件事，不再改写原版语义：
 * 1. 保留原生 baseline，强度化速度只在其上叠加 `2^(强化数 + 1)`（与 20.1-1.0.1 及更早版本一致）；
 * 2. 给 `baseOperations * baseline` 留出 int 余量，避免高速度时乘出负数导致机器完全不动。
 *
 * 20.1-1.0.3 / 21.1-1.0.9 曾把 baseline 换成「小机器同口径」，插满原版速度时操作数从
 * 4,194,304 掉到 448（大型回旋只剩小型回旋的速度），所以这里恢复原版语义。
 *
 * 原生语义下大型机器的操作数本来就极大，能量不够时实际执行数会被卡成
 * `容量 / 每操作耗能`，能耗栏出现与容量同量级的数字；这是「更多机器」自身的设定，
 * 想压低数值只能牺牲速度。
 */
fun TileEntityMekanism.modifyRecalculationBaselineMaxOperations(original: Double): Double {
    val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
    val empoweredSpeed = getInstalled(MekEmpUpgrade.EMPOWERED_SPEED) ?: 0

    // 与 MekMM 的 baseOperations 同形：4 * (原版速度 > 0 ? 原版速度 : 1)
    val baseOperations = 4.0 * max(1, vanillaSpeed)
    // 乘回去时仍然是 int，留一半余量，防止高速度下溢出成负数
    val safeBaseline = Int.MAX_VALUE.toDouble() / baseOperations / 2.0

    // 只改一件事：给原生 baseline 叠加强化速度的增量
    val boosted = if (isSpeedMaxed() && empoweredSpeed > 0) {
        original + 2.0 * 2.0.pow(empoweredSpeed.toDouble())
    } else {
        original
    }
    val baseline = min(boosted, safeBaseline)

    if (System.getProperty("mekanism_empowered.debug") != null) {
        MekanismEmpoweredCore.LOGGER.info(
            "[large-machine] vanillaSpeed={} empoweredSpeed={} baseOperations={} baseline={} -> operations={} (native {})",
            vanillaSpeed, empoweredSpeed, baseOperations, baseline,
            baseOperations * baseline.toInt(), original
        )
    }

    return baseline
}
