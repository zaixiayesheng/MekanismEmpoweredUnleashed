package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import dev.lapis256.mekanism_empowered.core.extension.getInstalled
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import mekanism.api.Upgrade
import mekanism.common.config.MekanismConfig
import mekanism.common.tile.base.TileEntityMekanism
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow


fun TileEntityMekanism.prcRecalculateAdditionalUpgrades() {
    recalculateUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)
}

/**
 * 大型机器（通用机械：更多机器里的那几台）把「每 tick 操作数」拆成
 * `baseOperations * baselineMaxOperations` 两个 int 相乘，原版 baseline 是 `2^原版速度`。
 * 速度一拉高这个指数就撑爆 int：到 2 级时 baseline 先饱和成 2.14G，再乘 baseOperations
 * 直接溢出成负数，Mekanism 拿负数去算 `storedEnergy / energyPerTick` 得到 0，于是同时
 * 报「能量不足」和「不足以最高速度运行」并强制停机。
 *
 * 这里把 baseline 换成和小机器同一口径：`每秒操作数 = 倍率^(总速度/8)`，再除以它自己的
 * baseOperations（`4 * max(1, 原版速度)`），让它乘回去的结果正好落在小机器的水平上；
 * 同时对 int 上限留出余量，绝不再溢出。
 */
fun TileEntityMekanism.modifyRecalculationBaselineMaxOperations(original: Double): Double {
    if (!isSpeedMaxed()) {
        return original
    }
    val speed = getInstalled(MekEmpUpgrade.EMPOWERED_SPEED) ?: return original
    if (speed <= 0) {
        return original
    }

    val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
    // 与 MekMM 的 baseOperations 同形：4 * (原版速度 > 0 ? 原版速度 : 原版速度 + 1)
    val baseOperations = 4.0 * max(1, vanillaSpeed)
    val targetOperations = MekanismConfig.general.maxUpgradeMultiplier.get().toDouble()
        .pow((vanillaSpeed + speed) / 8.0)
    // 乘回去时仍然是 int，留一半余量，防止对方的 baseOperations 比预估更大时溢出
    val safeBaseline = Int.MAX_VALUE.toDouble() / baseOperations / 2.0
    val baseline = min(targetOperations / baseOperations, safeBaseline)

    if (System.getProperty("mekanism_empowered.debug") != null) {
        MekanismEmpoweredCore.LOGGER.info(
            "[large-machine] speed={} (vanilla {} + empowered {}) baseOperations={} targetPerTick={} baseline={} -> operations={}",
            vanillaSpeed + speed, vanillaSpeed, speed, baseOperations, targetOperations, baseline,
            baseOperations * baseline.toInt()
        )
    }

    return baseline
}
