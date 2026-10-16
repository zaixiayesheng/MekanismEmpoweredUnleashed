package dev.lapis256.mekanism_empowered.mixin.unleashed

import com.llamalad7.mixinextras.injector.ModifyExpressionValue
import com.llamalad7.mixinextras.injector.ModifyReturnValue
import com.llamalad7.mixinextras.sugar.Local
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.api.Upgrade
import org.objectweb.asm.Opcodes
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.injection.At
import java.util.Locale

/**
 * 升级上限中枢：让速度/能量升级的上限**可配置**（原版线 8-32 默认 16，
 * 强化线 1-32 默认 16，两条线各自独立）。
 *
 * 实现方式是在运行时改写 `Upgrade#getMax()` 的返回值，
 * 并额外接管 1.20.1 读档路径里那个**不经过 getMax()、直接读 `maxStack` 字段**的夹取
 * （见下方 companion 里的 `mekanismUnleashedReadMax`）：
 *
 * - 相比原版 Unleashed 的"枚举构造器补丁"（在类加载期修改
 *   maxStack 参数），本方案**每次调用时读配置**，配置修改即时
 *   生效，也没有类加载时序问题；
 * - 原版 SPEED/ENERGY 走 [UnleashedUtils.maxUpgrades]，强化
 *   EMPOWERED_SPEED/EMPOWERED_ENERGY 走
 *   [UnleashedUtils.maxEmpoweredUpgrades]（按枚举常量名匹配，
 *   大小写都兼容）；
 * - Empowered 的第二层解锁判断（`isSpeedMaxed()` 比较
 *   `Upgrade.SPEED.max`）跟随原版线的上限——"插满 N 解锁"
 *   的 N 就是 maxUpgrades；
 * - 配置未加载时（注册表阶段）两个读取都会安全回退到默认值 16。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [Upgrade::class], remap = false)
class MixinUpgradeMax {

    /** 改写 getMax()：受控的四条升级线返回各自配置值，其余原样。 */
    @ModifyReturnValue(method = ["getMax"], at = [At("RETURN")])
    fun mekanismUnleashedScaledMax(original: Int): Int {
        val self = this as Upgrade
        return when {
            !isScaled(self.name) -> original
            isEmpowered(self.name) -> UnleashedUtils.maxEmpoweredUpgrades()
            else -> UnleashedUtils.maxUpgrades()
        }
    }

    /*
     * 注意：Kotlin 的 companion object 会生成非私有的静态字段，
     * 违反 Mixin 的硬规则，所以必须声明为 private companion。
     */
    private companion object {

        /** 受配置控制上限的四条升级线（枚举常量名）。 */
        private val SCALED_UPGRADES = setOf("SPEED", "ENERGY", "EMPOWERED_SPEED", "EMPOWERED_ENERGY")

        /** 其中属于第二层（强化）的两条，走独立的上限配置。 */
        private val EMPOWERED_UPGRADES = setOf("EMPOWERED_SPEED", "EMPOWERED_ENERGY")

        /**
         * 读档路径的上限：Mekanism 10.4（1.20.1）的 `Upgrade.buildMap(CompoundTag)`
         * 读存档时用**字段** `maxStack` 夹取数量 —— 反编译可见
         * `Mth.clamp(compound.getInt("amount"), 0, upgrade.maxStack)` —— 不经过
         * `getMax()`，所以上面那个 [mekanismUnleashedScaledMax] 管不到它。
         *
         * 症状就是「插了 16 个，重进存档只剩 8 个」（8 正是枚举里写死的 maxStack）。
         * 1.21.1（Mekanism 10.7）的存档走 Codec，没有这个问题。
         *
         * `buildMap` 是静态方法，handler 必须静态 —— Kotlin 里只能放 companion + @JvmStatic。
         * `require = 1`：这个注入点必须成立，宁可启动报错也不要悄悄退回 8。
         */
        @JvmStatic
        @ModifyExpressionValue(
            method = ["buildMap"],
            at = [At(value = "FIELD", target = "Lmekanism/api/Upgrade;maxStack:I", opcode = Opcodes.GETFIELD)],
            require = 1
        )
        private fun mekanismUnleashedReadMax(original: Int, @Local upgrade: Upgrade): Int =
            when {
                !isScaled(upgrade.name) -> original
                isEmpowered(upgrade.name) -> UnleashedUtils.maxEmpoweredUpgrades()
                else -> UnleashedUtils.maxUpgrades()
            }

        /** 大小写都兼容的匹配（新增升级的枚举名大小写取决于扩展加载器）。 */
        private fun isScaled(name: String): Boolean =
            name in SCALED_UPGRADES || name.uppercase(Locale.ROOT) in SCALED_UPGRADES

        private fun isEmpowered(name: String): Boolean =
            name in EMPOWERED_UPGRADES || name.uppercase(Locale.ROOT) in EMPOWERED_UPGRADES
    }
}
