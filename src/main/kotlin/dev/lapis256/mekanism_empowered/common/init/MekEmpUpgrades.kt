package dev.lapis256.mekanism_empowered.common.init

import com.jerry.mekanism_extras.common.tile.machine.TileEntityAdvancedElectricPump
import dev.lapis256.mekanism_empowered.api.MekEmpUpgrade
import dev.lapis256.mekanism_empowered.unleashed.UnleashedUtils
import dev.lapis256.mekanism_empowered.common.factory.MekanismFactoryTypeKeys
import dev.lapis256.mekanism_empowered.common.factory.addDeferredSupportedForFactory
import dev.lapis256.mekanism_empowered.core.common.upgrade.UpgradeInfoFunction
import dev.lapis256.mekanism_empowered.core.common.upgrade.UpgradeInfoHandler
import dev.lapis256.mekanism_empowered.core.common.util.AdditionalUpgradeUtil.addSupported
import dev.lapis256.mekanism_empowered.core.common.util.TileUpgradeSupportFallbackRegistry
import dev.lapis256.mekanism_empowered.core.extension.fractionUpgrades
import dev.lapis256.mekanism_empowered.extension.*
import dev.lapis256.mekanism_empowered.integration.mods.MekExt
import dev.lapis256.mekanism_empowered.mixin.common.AccessorBasicInventorySlot
import mekanism.api.Upgrade
import mekanism.common.MekanismLang
import mekanism.common.block.attribute.Attribute
import mekanism.common.block.attribute.AttributeUpgradeSupport
import mekanism.common.inventory.container.slot.ContainerSlotType
import mekanism.common.registries.MekanismBlockTypes
import mekanism.common.tile.interfaces.IUpgradeTile
import mekanism.common.tile.machine.TileEntityElectricPump
import net.minecraft.network.chat.Component
import kotlin.math.pow


object MekEmpUpgrades {
    val SPEED_AND_ENERGY_UPGRADES = arrayOf(MekEmpUpgrade.EMPOWERED_SPEED, MekEmpUpgrade.EMPOWERED_ENERGY)
    val MACHINE_UPGRADES = arrayOf(*SPEED_AND_ENERGY_UPGRADES, MekEmpUpgrade.IO_CAPACITY, MekEmpUpgrade.AUTO_INSERTER)
    val ITEM_INPUT_MACHINE_UPGRADES = arrayOf(*MACHINE_UPGRADES, MekEmpUpgrade.FAST_ITEM_INSERT)
    val ITEM_OUTPUT_MACHINE_UPGRADES = arrayOf(*MACHINE_UPGRADES, MekEmpUpgrade.FAST_ITEM_EJECT)
    val ITEM_IN_OUT_MACHINE_UPGRADES = arrayOf(*MACHINE_UPGRADES, MekEmpUpgrade.FAST_ITEM_INSERT, MekEmpUpgrade.FAST_ITEM_EJECT)

    fun registerUpgradeInfo() {
        // 泵：显示第二层（强化）倍率口径，与 1.21.1 合并版一致。
        // 注意 1.20.1 的泵自身还有两条公式（见 MixinImplTileEntityElectricPump）：
        // modifyWaterOutputAmount = 1 + 8×进度（+100%/级，满级 +800%）、
        // modifyOutputRate = 4^(4×进度)；旧文案 "已装×100%" 恰好等于前者。
        // 1.21.1 改成倍率口径，是因为那边第二层速度线还会乘到泵的吞吐上。
        val empoweredSpeedUpgradePumpInfo = { tile: IUpgradeTile ->
            val factor = UnleashedUtils.empoweredMultiplier(tile, MekEmpUpgrade.EMPOWERED_SPEED)
            listOf(Component.literal("Effect: +" + Math.round((factor - 1.0) * 100) + "%"))
        }

        // 显示真实数值：插了 n 个时效果 = 倍率^(n / 上限)，与数值中枢
        // （mixin_impl/MixinImplMekanismUtils）用 fractionUpgrades 算出来的完全一致。
        // 原版的 UpgradeUtils::getExpScaledInfo / getMultScaledInfo 在 Mekanism 10.4 里分别是
        // Math.pow(2, 已装数) 和按原版倍率的口径，都对不上强化后的实际值。
        val empoweredSpeedInfo: UpgradeInfoFunction = { tile, upgrade ->
            listOf(
                MekanismLang.UPGRADES_EFFECT.translate(
                    UnleashedUtils.exponential(UnleashedUtils.empoweredMultiplier(tile, upgrade))
                )
            )
        }

        UpgradeInfoHandler.register(MekEmpUpgrade.EMPOWERED_SPEED, empoweredSpeedInfo)
            .registerOverrideForTiles(TileEntityElectricPump::class) { it, _ -> empoweredSpeedUpgradePumpInfo.invoke(it) }
            .conditionallyRegisterOverride(MekExt.isLoaded) {
                registerOverrideForTiles(TileEntityAdvancedElectricPump::class) { it, _ -> empoweredSpeedUpgradePumpInfo.invoke(it) }
            } // TODO: Move to MekExt integration

        UpgradeInfoHandler.register(MekEmpUpgrade.EMPOWERED_ENERGY, empoweredSpeedInfo)

        UpgradeInfoHandler.register(MekEmpUpgrade.IO_CAPACITY) { tile, _ ->
            // 与 TileComponentInserter.getIOCapacity 同一套公式：进度 = 已装/上限，两种类型分开显示。
            // 流体/能量/化学 = 1 + 32 × 进度；物品 = 8^进度。
            // 此前写死的 "已装 × 3_200%" 把满级总效果当成每级效果，整体夸大 8 倍。
            val ratio = tile.fractionUpgrades(MekEmpUpgrade.IO_CAPACITY)
            listOf(
                Component.literal("Effect: +" + Math.round(3_200 * ratio) + "% (fluid/energy/chemical)"),
                Component.literal("Effect: +" + Math.round((8.0.pow(ratio) - 1.0) * 100) + "% (item)")
            )
        }
    }

    fun registerSupportedUpgrades() {
        val qioUpgrades = arrayOf(MekEmpUpgrade.EMPOWERED_SPEED, MekEmpUpgrade.IO_CAPACITY)

        addSupported(MekanismBlockTypes.ENRICHMENT_CHAMBER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.CRUSHER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.ENERGIZED_SMELTER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.PRECISION_SAWMILL, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.OSMIUM_COMPRESSOR, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.COMBINER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.METALLURGIC_INFUSER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.PURIFICATION_CHAMBER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.CHEMICAL_INJECTION_CHAMBER, *ITEM_IN_OUT_MACHINE_UPGRADES)

        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.ENRICHING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.CRUSHING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.SMELTING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.SAWING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.COMPRESSING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.COMBINING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.INFUSING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.PURIFYING, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addDeferredSupportedForFactory(MekanismFactoryTypeKeys.INJECTING, *ITEM_IN_OUT_MACHINE_UPGRADES)

        addSupported(MekanismBlockTypes.PRESSURIZED_REACTION_CHAMBER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.FORMULAIC_ASSEMBLICATOR, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.NUTRITIONAL_LIQUIFIER, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.PAINTING_MACHINE, *ITEM_IN_OUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.ANTIPROTONIC_NUCLEOSYNTHESIZER, *ITEM_IN_OUT_MACHINE_UPGRADES)

        addSupported(MekanismBlockTypes.CHEMICAL_OXIDIZER, *ITEM_INPUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.CHEMICAL_DISSOLUTION_CHAMBER, *ITEM_INPUT_MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.PIGMENT_EXTRACTOR, *ITEM_INPUT_MACHINE_UPGRADES)

        addSupported(MekanismBlockTypes.CHEMICAL_CRYSTALLIZER, *ITEM_OUTPUT_MACHINE_UPGRADES)

        addSupported(MekanismBlockTypes.CHEMICAL_INFUSER, *MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.CHEMICAL_WASHER, *MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.ROTARY_CONDENSENTRATOR, *MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.ELECTROLYTIC_SEPARATOR, *MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.ISOTOPIC_CENTRIFUGE, *MACHINE_UPGRADES)
        addSupported(MekanismBlockTypes.PIGMENT_MIXER, *MACHINE_UPGRADES)

        addSupported(MekanismBlockTypes.QIO_IMPORTER, *qioUpgrades, MekEmpUpgrade.FAST_ITEM_INSERT)
        addSupported(MekanismBlockTypes.QIO_EXPORTER, *qioUpgrades, MekEmpUpgrade.FAST_ITEM_EJECT)

        addSupported(MekanismBlockTypes.DIGITAL_MINER, *SPEED_AND_ENERGY_UPGRADES)
        addSupported(MekanismBlockTypes.ELECTRIC_PUMP, *SPEED_AND_ENERGY_UPGRADES)

        TileUpgradeSupportFallbackRegistry.registerSupportedUpgradeProvider { tile ->
            buildSet {
                run {
                    val attribute = Attribute.get(tile.blockType, AttributeUpgradeSupport::class.java) ?: return@run
                    val upgrades = attribute.supportedUpgrades

                    if (upgrades.contains(Upgrade.SPEED)) {
                        add(MekEmpUpgrade.EMPOWERED_SPEED)
                    }

                    if (upgrades.contains(Upgrade.ENERGY)) {
                        add(MekEmpUpgrade.EMPOWERED_ENERGY)
                    }
                }

                run {
                    if (
                        tile.gasHandlerManager != null ||
                        tile.infusionHandlerManager != null ||
                        tile.pigmentHandlerManager != null ||
                        tile.slurryHandlerManager != null ||
                        tile.fluidHandlerManager != null
                    ) {
                        add(MekEmpUpgrade.AUTO_INSERTER)
                        add(MekEmpUpgrade.IO_CAPACITY)
                    }

                    tile.itemHandlerManager?.getContainers(null)
                        ?.asSequence()
                        ?.mapNotNull { s -> (s as? AccessorBasicInventorySlot)?.getSlotType() }
                        ?.distinct()
                        ?.forEach { slotType ->
                            when (slotType) {
                                // エネルギースロットのみの場合は無視する。発電機等で大抵対応する必要がないので
                                ContainerSlotType.INPUT, ContainerSlotType.EXTRA -> {
                                    add(MekEmpUpgrade.IO_CAPACITY)
                                    add(MekEmpUpgrade.AUTO_INSERTER)
                                    add(MekEmpUpgrade.FAST_ITEM_INSERT)
                                }

                                ContainerSlotType.OUTPUT -> add(MekEmpUpgrade.FAST_ITEM_EJECT)

                                else -> {}
                            }
                        }
                }
            }
        }
    }
}

