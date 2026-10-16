package dev.lapis256.mekanism_empowered.common.config

import dev.lapis256.easy_nest_config.api.Comment
import dev.lapis256.easy_nest_config.api.NestConfig
import dev.lapis256.mekanism_empowered.core.common.config.MekanismNestConfig
import net.minecraftforge.fml.config.ModConfig


object MekEmpGeneralConfig : MekanismNestConfig(ModConfig.Type.SERVER, "general") {
    val maxUpgradeMultiplier by builder.comment("Base factor for working out machine performance with upgrades - UpgradeModifier * (UpgradesInstalled/UpgradesPossible).")
        .defineInRange("maxUpgradeMultiplier", 20, 1, Int.MAX_VALUE).cached()

    // ↓↓↓ 以下 7 项由「解放（Unleashed）」那一半引入（合并自 Mekanism Unleashed / WhitePhant0m）。
    // 键名、默认值、取值范围与 1.21.1 合并版逐字对齐；改这里之前先看那边的 MekEmpGeneralConfig.kt。
    // 1.20.1 没有 Mekanism 的配置界面，所以 builder.comment 里的说明就是玩家唯一能看到的中文文档。

    // 原版速度/能量升级的安装上限。它同时决定强化线何时解锁：原版那条线先插满。
    val maxUpgrades by builder.comment("机器可安装的速度/能量升级最大数量（8-32），默认 16。原版速度/能量升级插满后，才会启用对应的强化升级。")
        .defineInRange("maxUpgrades", 16, 8, 32).cached()

    // 强化速度/能量升级有自己的上限，两层因此可以独立配置。它只决定强化加成被拆成几级；
    // 插满时的总加成来自 maxUpgradeMultiplier，并按 maxUpgrades 稀释。
    val maxEmpoweredUpgrades by builder.comment("机器可安装的强化速度/能量升级最大数量（1-32），默认 16。只决定强化层的总加成被拆成几级；插满时的总加成由「最大升级倍率」决定。")
        .defineInRange("maxEmpoweredUpgrades", 16, 1, 32).cached()

    // 抵消率：速度升级带来的额外耗能里，能量升级能抵掉多少。1.0 是原版行为（能量升级数与
    // 速度升级数相同时，每操作耗电回到基础值）；调低即整条线更费电。整合包/服务器拿它当难度旋钮。
    val vanillaEnergyOffsetRate by builder.comment("原版能量升级能抵掉多少比例的“提速额外耗能”（0.0-1.0），默认 1.0。1.0 即原版行为：能量与速度升级数量相同时，每操作耗电回到基础值；调低即整条线更费电。")
        .defineInRange("vanillaEnergyOffsetRate", 1.0, 0.0, 1.0).cached()

    // 强化能量线同款旋钮。默认 0.5：插满强化能量线只抵掉强化速度线耗能的一半，
    // 强化层故意不做成能量中性。填 1.0 可恢复旧行为。
    val empoweredEnergyOffsetRate by builder.comment("强化能量升级能抵掉多少比例的“强化提速额外耗能”（0.0-1.0），默认 0.5。默认只抵掉一半，插满强化线后每操作耗电高于原版；填 1.0 完全抵消。")
        .defineInRange("empoweredEnergyOffsetRate", 0.5, 0.0, 1.0).cached()

    val enchantableMekaGear by builder.comment("允许 Meka 装甲/工具被附魔，默认 false。你需要自行把物品加入可附魔标签。")
        .define("enchantableMekaGear", false).cached()

    // 合并自 Mekanism Unleashed：自带补次循环的四台机器（电动泵、数字矿机、流体装填器、公式装配器）
    // 原本硬编码 128 次/tick，超过这个值的速度升级会被静默吞掉。现在可配置，默认仍是 128。
    val maxOperationsPerTick by builder.comment("电动泵、数字矿机、流体装填器、公式装配器每 tick 的补次上限（1-10000），默认 128。速度升级超过这个值后，对这四台机器不再有可见效果；调得过高会拖慢服务器。")
        .defineInRange("maxOperationsPerTick", 128, 1, 10000).cached()

    // 开启时（默认）上面这个上限会自动跟着 maxUpgrades 抬高（只升不降），
    // 所以只调 maxUpgrades 就能让速度升级在那四台机器上继续有意义；关闭后该项即硬上限。
    val autoScaleOperationsPerTick by builder.comment("开启时（默认）「每 tick 最大操作次数」自动跟随「最大升级数」抬高，只升不降；关闭后该项即成为硬上限。")
        .define("autoScaleOperationsPerTick", true).cached()

    @NestConfig
    @Comment("Configurable Auto Inserter")
    object AutoInserter {
        val itemRate by builder.comment("Configurable item rate for Auto Inserter")
            .defineInRange("itemRate", 8, 1, 64).cached()
        val chemicalRate by builder.comment("Configurable chemical rate for Auto Inserter")
            .defineInRange("chemicalRate", 128L, 1L, Long.MAX_VALUE).cached()
        val fluidRate by builder.comment("Configurable fluid rate for Auto Inserter")
            .defineInRange("fluidRate", 128, 1, Int.MAX_VALUE).cached()
        val energyRate by builder.comment("Configurable energy rate for Auto Inserter")
            .defineInRange("energyRate", 128L, 1L, Long.MAX_VALUE).cached()
    }
}
