package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.math.MathUtils
import mekanism.common.attachments.component.UpgradeAware
import kotlin.math.pow


class MixinImplUpgradeBasedUnsignedLongCache {
    fun modifyMaxEnergy(upgradeAware: UpgradeAware, base: Long): Long {
        val empoweredInstalled = upgradeAware.getUpgradeCount(MekEmpUpgrade.EMPOWERED_ENERGY)
        if (empoweredInstalled <= 0) {
            return base
        }

        // base 是调用点刚传进来的值（原版按「原版能量升级数」算出的容量，
        // 原版自己带缓存）。这里每次都按它重算，不能只把强化升级数当失效键：
        // 原版那一路变化时旧键不变，会一直返回拆升级之前的老值，
        // tooltip 显示的容量和实际容量对不上。
        return MathUtils.clampToLong(
            base * UnleashedUtils.empoweredMultiplierBase()
                .pow(empoweredInstalled / MekEmpUpgrade.EMPOWERED_ENERGY.max.toDouble())
        )
    }
}
