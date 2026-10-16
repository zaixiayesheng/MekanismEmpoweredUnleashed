package dev.lapis256.mekanism_empowered.core.mixin_impl

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.UnboundedMapCodec
import dev.lapis256.mekanism_empowered.core.api.MekEmpCoreSerializationConstants
import dev.lapis256.mekanism_empowered.core.api.upgrade.AdditionalUpgradeLoader
import dev.lapis256.mekanism_empowered.core.common.MekanismEmpoweredCore
import dev.lapis256.mekanism_empowered.core.extension.getCompoundOrNull
import dev.lapis256.mekanism_empowered.core.extension.getIntOrNull
import dev.lapis256.mekanism_empowered.core.extension.getTypedListOrNull
import mekanism.api.SerializationConstants
import mekanism.api.Upgrade
import mekanism.api.text.EnumColor
import mekanism.api.text.ILangEntry
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtOps
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.StringRepresentable
import java.util.*


class MixinImplUpgrade(constructor: (String, Int, String, ILangEntry, ILangEntry, Int, EnumColor) -> Upgrade) {
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

    // vanillaLastOrdinal 是「追加的第一个升级的序号」（= 追加前枚举常量的个数），
    // 所以区间必须从它本身开始：写 `+ 1` 会把第一个追加的升级（EMPOWERED_SPEED）关在区间外，
    // 让它永远留在原版 upgrades 列表里、永远写不进自己的分区。1.21.1 上实测确认。
    private val additionalOrdinalRange: IntRange by lazy { vanillaLastOrdinal..additionalLastOrdinal }

    private val loader = AdditionalUpgradeLoader(constructor)

    private var _codec: Codec<Upgrade>? = null
    private var codec: Codec<Upgrade>
        get() = _codec ?: error("Codec not initialized")
        set(value) = _codec
            ?.let { error("Codec is already initialized") }
            ?: run { _codec = value }

    private val additionalCodec: UnboundedMapCodec<Upgrade, Int> by lazy {
        Codec.unboundedMap(codec, ExtraCodecs.POSITIVE_INT)
    }

    /**
     * Registers the added upgrades to [Upgrade].
     *
     * 追加されたアップグレードを [Upgrade] に登録します。
     */
    fun initAdditionalUpgrades(builtInUpgrades: Array<Upgrade>): Array<Upgrade> {
        vanillaLastOrdinal = builtInUpgrades.size
        return loader.initAdditionalEnumEntry(builtInUpgrades)
            .also {
                additionalLastOrdinal = it.size - 1
                codec = StringRepresentable.fromEnum { it }
            }
    }

    /**
     * Removes elements corresponding to upgrades added from [SerializationConstants.UPGRADES].
     * This is to avoid conflicts with upgrades added by other mods, such as when this mod is installed later.
     * Although there is a possibility that already installed upgrades may be lost, priority is given to upgrades protected by this mod.
     *
     * If other mod developers are reading this message, please consider saving to your own tag like [saveAdditionalMap] / [buildAdditionalMap].
     *
     *
     * [SerializationConstants.UPGRADES] から追加されたアップグレードに該当する要素を削除します。
     * これは、この Mod が後からインストールされた場合などに、他の Mod によって追加されたアップグレードとの競合を避けるためです。
     * 既にインストールされているアップグレードが失われる可能性がありますが、この Mod によって保護されているアップグレードを優先します。
     *
     * 他のMod開発者の方がこのメッセージを読んでいる場合は、ぜひ [saveAdditionalMap] / [buildAdditionalMap] のように独自のタグに保存することを検討してください。
     */
    private fun cleanupAdditionalUpgrade(nbtTags: CompoundTag) {
        nbtTags.getTypedListOrNull<CompoundTag>(SerializationConstants.UPGRADES)?.removeIf {
            val type = it.getIntOrNull(SerializationConstants.TYPE) ?: return@removeIf false
            return@removeIf type in additionalOrdinalRange
        }
    }

    /**
     * Migration helper for saves written by versions with the off-by-one [additionalOrdinalRange]
     * (1.0.6 and older): back then the first additional upgrade was never stored in our own tag,
     * it stayed in the vanilla [SerializationConstants.UPGRADES] list. Now that the range covers it,
     * [cleanupAdditionalUpgrade] would delete that entry, so pick it up before cleaning up.
     *
     * @return the vanilla entry when our own tag does not contain it yet, otherwise `null`
     *
     * 序号区间差一位的旧版本（1.0.6 及更早）从没把第一个追加的升级写进自己的分区，
     * 它一直留在原版 [SerializationConstants.UPGRADES] 列表里。现在区间把它包进来了，
     * [cleanupAdditionalUpgrade] 会把它从列表里删掉，所以要先把它捡出来，避免老存档丢数量。
     */
    private fun salvageVanillaUpgrade(nbtTags: CompoundTag, compound: CompoundTag): Pair<Upgrade, Int>? {
        val upgrade = Upgrade.BY_ID.apply(vanillaLastOrdinal) ?: return null
        if (compound.contains(upgrade.serializedName)) return null

        return nbtTags.getTypedListOrNull<CompoundTag>(SerializationConstants.UPGRADES)
            ?.firstNotNullOfOrNull { tag ->
                val type = tag.getIntOrNull(SerializationConstants.TYPE) ?: return@firstNotNullOfOrNull null
                if (type != vanillaLastOrdinal) return@firstNotNullOfOrNull null

                val amount = tag.getIntOrNull(SerializationConstants.AMOUNT) ?: return@firstNotNullOfOrNull null
                if (amount <= 0) null else upgrade to amount
            }
    }

    /**
     * Loads additional upgrades from [MekEmpCoreSerializationConstants.UPGRADES].
     *
     * [MekEmpCoreSerializationConstants.UPGRADES] から追加のアップグレードを読み込みます。
     */
    fun buildAdditionalMap(upgrades: MutableMap<Upgrade, Int>?, nbtTags: CompoundTag?): MutableMap<Upgrade, Int>? {
        nbtTags ?: return upgrades

        val compound = nbtTags.getCompoundOrNull(MekEmpCoreSerializationConstants.UPGRADES)
            ?: nbtTags.getCompoundOrNull(MekEmpCoreSerializationConstants.UPGRADES_OLD)
            // 没有自己的分区就什么都别动：旧版本可能把升级留在原版列表里，清掉就等于把数量删了。
            ?: return upgrades

        val salvaged = salvageVanillaUpgrade(nbtTags, compound)

        cleanupAdditionalUpgrade(nbtTags)

        val upgrades = upgrades ?: EnumMap(Upgrade::class.java)

        additionalCodec.parse(NbtOps.INSTANCE, compound)
            .ifSuccess(upgrades::putAll)
            .ifError { e -> MekanismEmpoweredCore.LOGGER.error("Failed to parse additional upgrades: {}", e) }

        // 分区里没有的那一份（老存档只存在原版列表里的）兜底填回去，分区里的优先。
        salvaged?.let { (upgrade, amount) -> upgrades.putIfAbsent(upgrade, amount) }

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
                additionalCodec.encodeStart(NbtOps.INSTANCE, additional.associate { it.key to it.value })
                    .ifSuccess { tag -> nbtTags.put(MekEmpCoreSerializationConstants.UPGRADES, tag) }
                    .ifError { e -> MekanismEmpoweredCore.LOGGER.error("Failed to save additional upgrades: {}", e) }
                return@let vanilla.toSet()
            }

//    fun buildAdditionalMap(upgrades: MutableMap<Upgrade, Int>?, nbtTags: CompoundTag?): MutableMap<Upgrade, Int>? {
//        nbtTags ?: return null
//
//        if (!nbtTags.contains(MekanismEmpoweredCore.SerializationConstants.UPGRADES, Tag.TAG_COMPOUND.toInt())) {
//            return null
//        }
//
//        val upgrades = upgrades ?: EnumMap(Upgrade::class.java)
//
//        additionalCodec.parse(
//            NbtOps.INSTANCE,
//            nbtTags.getCompound(MekanismEmpoweredCore.SerializationConstants.UPGRADES)
//        )
//            .ifSuccess(upgrades::putAll)
//            .ifError { e -> MekanismEmpoweredCore.LOGGER.error("Failed to parse additional upgrades: {}", e) }
//
//        return upgrades
//    }
//
//    fun filterUpgrades(original: Set<Map.Entry<Upgrade, Int>>): Set<Map.Entry<Upgrade, Int>> {
//        return original
//            .filter { e -> !additionalOrdinals.contains(e.key.ordinal) }
//            .toSet()
//    }
//
//    fun saveAdditionalMap(upgrades: Map<Upgrade, Int>, nbtTags: CompoundTag) {
//        val additionalUpgrades = upgrades
//            .filter { e -> additionalOrdinals.contains(e.key.ordinal) }
//
//        additionalCodec.encodeStart(NbtOps.INSTANCE, additionalUpgrades)
//            .ifSuccess { tag -> nbtTags.put(MekanismEmpoweredCore.SerializationConstants.UPGRADES, tag) }
//            .ifError { e -> MekanismEmpoweredCore.LOGGER.error("Failed to save additional upgrades: {}", e) }
//    }
}
