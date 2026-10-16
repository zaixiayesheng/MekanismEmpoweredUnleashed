package dev.lapis256.mekanism_empowered.mixin_impl

import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.core.extension.fractionUpgrades
import dev.lapis256.mekanism_empowered.core.extension.isSpeedMaxed
import mekanism.api.math.MathUtils
import mekanism.common.tile.interfaces.IUpgradeTile
import kotlin.math.pow


object MixinImplTileEntityElectricPump {
    @JvmStatic
    fun IUpgradeTile.modifyOutputRate(original: Int): Int {
        if (!isSpeedMaxed()) {
            return original
        }

        return MathUtils.clampToInt(original * 4.0.pow(4 * fractionUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)))
    }

    @JvmStatic
    fun IUpgradeTile.modifyWaterOutputAmount(original: Int): Int {
        if (!isSpeedMaxed()) {
            return original
        }

        // +100% ~ +800%
        return MathUtils.clampToInt(original * (1 + 8 * fractionUpgrades(MekEmpUpgrade.EMPOWERED_SPEED)))
    }
}
