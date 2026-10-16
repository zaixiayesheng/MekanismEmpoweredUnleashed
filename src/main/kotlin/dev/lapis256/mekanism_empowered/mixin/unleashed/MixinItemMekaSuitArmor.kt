package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.common.item.gear.ItemMekaSuitArmor
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin

/**
 * Meka 装甲：附魔开关（配置项 `enchantableMekaGear`）。
 *
 * 旧实现注入 `MekanismArmorMaterials#lambda$static$7`，把附魔性能写进
 * 装甲材质。但材质是在 `MekanismArmorMaterials` 类初始化时构造的，那一刻
 * SERVER 型配置必然还没加载，[UnleashedUtils.enchantableMekaGear] 只能回退
 * false —— 等于开关永远不生效；而且注入目标是编译期 lambda 编号，
 * Mekanism 版本一变就失效。
 *
 * 现在改成与 [MixinItemMekaTool] 同款的运行时判定：直接给装甲类补上两个
 * 覆写方法。原版 `Item#isEnchantable` 只判断"不可堆叠 + 有耐久"，装甲本来
 * 就返回 true，这里补上关闭时的拦截。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [ItemMekaSuitArmor::class])
abstract class MixinItemMekaSuitArmor {

    /** 覆写可附魔判定：直接返回配置开关。 */
    fun isEnchantable(stack: ItemStack): Boolean = UnleashedUtils.enchantableMekaGear()

    /** 附魔性能：开启时 30（金质水准），否则 0（与原版 MEKASUIT 材质一致）。 */
    fun getEnchantmentValue(): Int = if (UnleashedUtils.enchantableMekaGear()) 30 else 0
}
