package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.math.MathUtils
import mekanism.common.attachments.component.UpgradeAware
import kotlin.math.pow


class MixinImplUpgradeBasedUnsignedLongCache {
    private var lastEmpoweredInstalled = 0
    private var value = 0L

    fun modifyMaxEnergy(upgradeAware: UpgradeAware, base: Long): Long {
        val empoweredInstalled = upgradeAware.getUpgradeCount(MekEmpUpgrade.EMPOWERED_ENERGY)
        if (empoweredInstalled <= 0) {
            return base
        }

        if (lastEmpoweredInstalled != empoweredInstalled) {
            lastEmpoweredInstalled = empoweredInstalled
            // 与方块侧同一套倍率（含随原版上限自动稀释），
            // 避免物品形态与方块形态出现两个口径。
            value = MathUtils.clampToLong(base * UnleashedUtils.empoweredMultiplierBase().pow(empoweredInstalled / MekEmpUpgrade.EMPOWERED_ENERGY.max.toDouble()))
        }

        return value
    }
}
