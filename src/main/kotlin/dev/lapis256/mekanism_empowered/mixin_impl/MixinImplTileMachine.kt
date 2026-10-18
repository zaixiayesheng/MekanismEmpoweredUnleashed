package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import dev.lapis256.mekanism_empowered.core.extension.getInstalled
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.Upgrade
import mekanism.common.tile.base.TileEntityMekanism
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


fun TileEntityMekanism.prcRecalculateAdditionalUpgrades() {
    recalculateUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)
}

/**
 * 大型机器（通用机械：更多机器里的那几台）的「每 tick 操作数」是
 * `baseOperations * baselineMaxOperations` 两个 int 相乘。
 *
 * 这里把原版与强化两条速度线改成**共同瓜分同一个极限**：
 * 原版速度升级是核心，每级权重 3；强化速度升级每级权重 1。
 * 两者的贡献线性相加后再按各自的绝对上限 32 归一，得到 0~1 的进度，
 * 乘以速度极限（int 安全线的一半，1,073,741,823 次/t）就是当前档位的操作数。
 *
 * 因此：
 * - 32 原版 + 32 强化时进度为 1，速度摸到极限；
 * - 16 原版 + 16 强化时进度为 0.5，是两头都不占满的平均档；
 * - 8 原版 + 32 强化时强化占大头（进度约 0.44）；
 * - 原版越多，强化能匀到的份额越少，反之亦然。
 *
 * 强化速度升级和原版一样有前置条件：原版速度升级没插满时，强化速度不生效
 * （和小机器的规则一致），避免只插强化就白嫖到高速。
 *
 * 21.1-1.0.9 / 20.1-1.0.3 的「小机器同口径」把插满原版速度的操作数压到 448，
 * 大型回旋只剩小型回旋的速度；21.1-1.0.10 之前又恢复成 `2^原版 + 2^(强化+1)`
 * 的叠加式，32 档会顶到 int 上限、16 档却与极限差出几个数量级。
 */
fun TileEntityMekanism.modifyRecalculationBaselineMaxOperations(original: Double): Double {
    val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
    val empoweredSpeed = largeMachineEffectiveEmpoweredSpeed()
    if (vanillaSpeed <= 0 && empoweredSpeed <= 0) {
        return original
    }

    val baseOperations = 4.0 * max(1, vanillaSpeed)
    val baseline = largeMachineBaseline(vanillaSpeed, empoweredSpeed, baseOperations)

    if (System.getProperty("mekanism_empowered.debug") == "true") {
        MekanismEmpoweredCore.LOGGER.info(
            "[large-machine] {} @{} vanillaSpeed={} empoweredSpeed={} progress={} baseOperations={} baseline={} -> operations={} (native {})",
            this.javaClass.name, this.blockPos, vanillaSpeed, empoweredSpeed,
            largeMachineSpeedProgress(vanillaSpeed, empoweredSpeed), baseOperations, baseline,
            baseOperations * baseline.toInt(), original
        )
    }

    return baseline
}

/**
 * 大型机器当前档位每 tick 允许的操作数上限（与 [modifyRecalculationBaselineMaxOperations]
 * 写入的 baseline 相乘得到的结果一致），供能量补偿反推单价时复用。
 */
fun TileEntityMekanism.largeMachineOperationsPerTick(): Double {
    val vanillaSpeed = component.getUpgrades(Upgrade.SPEED)
    val empoweredSpeed = largeMachineEffectiveEmpoweredSpeed()
    val baseOperations = 4.0 * max(1, vanillaSpeed)
    return baseOperations * largeMachineBaseline(vanillaSpeed, empoweredSpeed, baseOperations)
}

/** 原版速度没插满时强化速度不生效，与小型机器的解锁规则保持一致。 */
private fun TileEntityMekanism.largeMachineEffectiveEmpoweredSpeed(): Int {
    if (!isSpeedMaxed()) {
        return 0
    }
    return getInstalled(MekEmpUpgrade.EMPOWERED_SPEED) ?: 0
}

private fun largeMachineSpeedProgress(vanillaSpeed: Int, empoweredSpeed: Int): Double {
    // 原版线是核心：它自己就能占掉极限的 75%；强化线只均摊剩下的 25%。
    // 归一基准是模组能开到的绝对上限 32，而不是玩家当前配置的上限——
    // 否则把上限调低后，16+16 也会被算成 100%，32 档就失去意义了。
    val vanillaShare = min(1.0, vanillaSpeed / ABSOLUTE_MAX_UPGRADES)
    val empoweredShare = min(1.0, empoweredSpeed / ABSOLUTE_MAX_UPGRADES)
    return min(1.0, vanillaShare * VANILLA_SPEED_SHARE + empoweredShare * (1.0 - VANILLA_SPEED_SHARE))
}

private fun largeMachineBaseline(vanillaSpeed: Int, empoweredSpeed: Int, baseOperations: Double): Double {
    val progress = largeMachineSpeedProgress(vanillaSpeed, empoweredSpeed)
    // 速度极限取 int 安全线的一半，乘回 baseOperations 后不会溢出
    val operationsLimit = Int.MAX_VALUE / 2.0
    return max(1.0, floor(operationsLimit * progress / baseOperations))
}

/** 原版速度升级每级权重，强化速度升级每级权重为 1，合起来线性瓜分同一个速度极限。 */
private const val VANILLA_SPEED_SHARE = 0.75

/** 模组支持的单条升级线绝对上限，速度极限按 32+32 定义。 */
private const val ABSOLUTE_MAX_UPGRADES = 32.0
