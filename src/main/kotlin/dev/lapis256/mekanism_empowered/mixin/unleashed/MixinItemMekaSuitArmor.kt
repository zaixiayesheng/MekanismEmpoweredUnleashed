package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.common.item.gear.ItemMekaSuitArmor
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin

/**
 * Meka 装甲：附魔开关（配置项 `enchantableMekaGear`）。
 *
 * 1.20.1 的 Mekanism 并没有覆写 `Item#isEnchantable` / `Item#getEnchantmentValue`
 * （那是 1.21.1 才有的），所以这里不能像 1.21.1 那样去改返回值，只能由 mixin
 * 直接给目标类补上两个覆写方法。
 *
 * 这样也顺带修掉了旧实现的老问题：旧实现注入 `MekanismArmorMaterials` 里的
 * 编译期 lambda 编号，而该材质是在类初始化时构造的——那一刻 SERVER 型配置
 * 必然还没加载，[UnleashedUtils.enchantableMekaGear] 只能回退 false，
 * 等于开关永远不生效，而且 Mekanism 版本一变编号就失效。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [ItemMekaSuitArmor::class], remap = false)
abstract class MixinItemMekaSuitArmor {

    /** 覆写可附魔判定：直接返回配置开关。 */
    fun isEnchantable(stack: ItemStack): Boolean = UnleashedUtils.enchantableMekaGear()

    /** 附魔性能：开启时 30（金质水准），否则 0（与原版 MEKASUIT 材质一致）。 */
    fun getEnchantmentValue(): Int = if (UnleashedUtils.enchantableMekaGear()) 30 else 0
}
