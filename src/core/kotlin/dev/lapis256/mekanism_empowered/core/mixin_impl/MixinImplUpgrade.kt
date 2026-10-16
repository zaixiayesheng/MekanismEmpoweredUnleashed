package dev.lapis256.mekanism_empowered.core.mixin_impl

import dev.lapis256.mekanism_empowered.core.api.MekEmpCoreSerializationConstants
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgradeLoader
import dev.lapis256.mekanism_empowered.core.extension.getCompoundOrNull
import dev.lapis256.mekanism_empowered.core.extension.getIntOrNull
import dev.lapis256.mekanism_empowered.core.extension.getTypedListOrNull
import dev.lapis256.mekanism_empowered.core.mixin.common.InvokeAPILang
import mekanism.api.NBTConstants
import mekanism.api.Upgrade
import mekanism.api.text.APILang
import mekanism.api.text.EnumColor
import mekanism.api.text.ILangEntry
import mekanism.common.util.EnumUtils
import net.minecraft.nbt.CompoundTag
import java.util.*


class MixinImplUpgrade(val constructor: (String, Int, String, APILang, APILang, Int, EnumColor) -> Upgrade) {
    private val upgradeMap by lazy { EnumUtils.UPGRADES.associateBy(Upgrade::getRawName) }

    private fun createDummy(key: ILangEntry) = InvokeAPILang.createDummy("MEKANISM_EMPOWERED_CORE_DUMMY_API_LANG", 999, key.translationKey)

    private var _vanillaLastOrdinal: Int? = null
    private var vanillaLastOrdinal: Int
        get() = _vanillaLastOrdinal ?: error("Vanilla last ordinal not set")
        set(value) = _vanillaLastOrdinal
            ?.let { error("Vanilla last ordinal is already set to $it, cannot set to $value") }
            ?: run { _vanillaLastOrdinal = value }

    private var _additionalLastOrdinal: Int? = null
    private var additionalLastOrdinal: Int
        get() = _additionalLastOrdinal ?: error("Additional last ordinal not set")
        set(value) = _additionalLastOrdinal
            ?.let { error("Additional last ordinal is already set to $it, cannot set to $value") }
            ?: run { _additionalLastOrdinal = value }

    // 【1.20.1 修复】vanillaLastOrdinal 是「追加升级的第一个序号」（= 追加前的枚举元素个数），
    // 不是「原版最后一个序号」。原来写成 (vanillaLastOrdinal + 1).. 会把第一个追加升级
    // （EMPOWERED_SPEED，序号 7）漏在范围外 —— 表现为它永远留在原版 upgrades 列表里、
    // 永不写进我们自己的标签，cleanup 也管不到它。
    // 实测依据：用 setblock 给机器写入 {upgrades:[{type:7,amount:9}]} 后，读回仍是原版列表里的 type 7。
    private val additionalOrdinalRange: IntRange by lazy { vanillaLastOrdinal..additionalLastOrdinal }

    private fun altConstructor(
        internalName: String,
        ordinal: Int,
        name: String,
        langKey: ILangEntry,
        descLangKey: ILangEntry,
        maxStack: Int,
        color: EnumColor
    ) =
        constructor(internalName, ordinal, name, createDummy(langKey), createDummy(descLangKey), maxStack, color)

    private val loader = AdditionalUpgradeLoader(::altConstructor)

    /**
     * Registers the added upgrades to [Upgrade].
     *
     * 追加されたアップグレードを [Upgrade] に登録します。
     */
    fun initAdditionalUpgrades(builtInUpgrades: Array<Upgrade>): Array<Upgrade> {
        vanillaLastOrdinal = builtInUpgrades.size
        return loader.initAdditionalEnumEntry(builtInUpgrades)
            .also { additionalLastOrdinal = it.size - 1 }
    }

    /**
     * Removes elements corresponding to upgrades added from [NBTConstants.UPGRADES].
     * This is to avoid conflicts with upgrades added by other mods, such as when this mod is installed later.
     * Although there is a possibility that already installed upgrades may be lost, priority is given to upgrades protected by this mod.
     *
     * If other mod developers are reading this message, please consider saving to your own tag like [saveAdditionalMap] / [buildAdditionalMap].
     *
     *
     * [NBTConstants.UPGRADES] から追加されたアップグレードに該当する要素を削除します。
     * これは、この Mod が後からインストールされた場合などに、他の Mod によって追加されたアップグレードとの競合を避けるためです。
     * 既にインストールされているアップグレードが失われる可能性がありますが、この Mod によって保護されているアップグレードを優先します。
     *
     * 他のMod開発者の方がこのメッセージを読んでいる場合は、ぜひ [saveAdditionalMap] / [buildAdditionalMap] のように独自のタグに保存することを検討してください。
     */
    private fun cleanupAdditionalUpgrade(nbtTags: CompoundTag) {
        nbtTags.getTypedListOrNull<CompoundTag>(NBTConstants.UPGRADES)?.removeIf {
            val type = it.getIntOrNull(NBTConstants.TYPE) ?: return@removeIf false
            return@removeIf type in additionalOrdinalRange
        }
    }

    /**
     * Loads additional upgrades from [MekEmpCoreSerializationConstants.UPGRADES].
     *
     * [MekEmpCoreSerializationConstants.UPGRADES] から追加のアップグレードを読み込みます。
     */
    fun buildAdditionalMap(upgrades: MutableMap<Upgrade, Int>?, nbtTags: CompoundTag?): MutableMap<Upgrade, Int>? {
        nbtTags ?: return null

        val compound = nbtTags.getCompoundOrNull(MekEmpCoreSerializationConstants.UPGRADES)
            ?: nbtTags.getCompoundOrNull(MekEmpCoreSerializationConstants.UPGRADES_OLD)

        // 迁移分支：存档里没有我们自己的标签，说明追加升级还留在原版列表里
        // （1.20.1 的 20.1-1.0.0 就是这个状态：saveAdditionalMap 当时静默失效）。
        // 此时绝对不能 cleanup —— 那会把它们整段删掉；直接原样交回，让原版读取器读出它们，
        // 等下一次保存时 saveAdditionalMap 会把它们搬进我们自己的标签。
        compound ?: return upgrades

        cleanupAdditionalUpgrade(nbtTags)

        val upgrades = upgrades ?: EnumMap(Upgrade::class.java)

        for (entry in compound.allKeys) {
            val upgrade = upgradeMap[entry] ?: continue
            val amount = compound.getInt(entry)
            if (amount > 0) {
                upgrades[upgrade] = amount
            }
        }

        return upgrades
    }

    /**
     * Saves additional upgrades to another tag.
     * Returns entries of upgrades removed from the original map.
     *
     * 追加のアップグレードを別のタグに保存します。
     * 元のマップから削除されたアップグレードのエントリを返します。
     */
    fun saveAdditionalMap(upgrades: Set<Map.Entry<Upgrade, Int>>, nbtTags: CompoundTag): Set<Map.Entry<Upgrade, Int>> =
        upgrades
            .partition { e -> e.key.ordinal in additionalOrdinalRange }
            .let { (additional, vanilla) ->
                nbtTags.put(MekEmpCoreSerializationConstants.UPGRADES, CompoundTag().apply {
                    for ((upgrade, amount) in additional) {
                        putInt(upgrade.rawName, amount)
                    }
                })
                return@let vanilla.toSet()
            }
}
