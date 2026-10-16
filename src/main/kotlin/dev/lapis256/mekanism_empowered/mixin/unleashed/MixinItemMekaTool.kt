package dev.lapis256.mekanism_empowered.mixin.unleashed

import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import mekanism.common.item.gear.ItemMekaTool
import net.minecraft.world.item.ItemStack
import org.spongepowered.asm.mixin.Mixin

/**
 * Meka 工具：附魔开关（配置项 `enchantableMekaGear`）。
 *
 * 与 [MixinItemMekaSuitArmor] 同理：1.20.1 的 Mekanism 没有覆写
 * `Item#isEnchantable` / `Item#getEnchantmentValue`，由 mixin 补上覆写方法。
 *
 * 配置未加载时（注册表阶段）由 [UnleashedUtils.enchantableMekaGear]
 * 安全回退为 false，保持原版行为。
 *
 * 合并自 Mekanism Unleashed (WhitePhant0m)。
 */
@Mixin(value = [ItemMekaTool::class], remap = false)
abstract class MixinItemMekaTool {

    /** 覆写可附魔判定：直接返回配置开关。 */
    fun isEnchantable(stack: ItemStack): Boolean = UnleashedUtils.enchantableMekaGear()

    /** 附魔性能：开启时 30（金质水准），否则 0。 */
    fun getEnchantmentValue(): Int = if (UnleashedUtils.enchantableMekaGear()) 30 else 0
}
