<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">通用机械：增强·解放</h1>

<h3 align="center">Mekanism: Empowered Unleashed</h3>

<div align="center">

通用机械（Mekanism）的升级扩展：把《通用机械：增强》与《通用机械：解放》合并成一个模组，在原版升级之上再加一层「强化」升级。

English version: [README_EN.md](./README_EN.md)

[![Release](https://img.shields.io/github/v/release/zaixiayesheng/MekanismEmpoweredUnleashed?filter=v20.1*&style=flat-square&label=Release&color=blue)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/releases)
[![CurseForge](http://cf.way2muchnoise.eu/1702076.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-green.svg?style=flat-square)](https://www.minecraft.net)
[![Forge 47.4](https://img.shields.io/badge/Forge-47.4-orange.svg?style=flat-square)](https://files.minecraftforge.net)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](./LICENSE)

</div>

<div align="center">

[这是什么](#这是什么) · [安装](#安装) · [两套升级](#两套升级) · [耗电怎么算](#耗电怎么算) · [大型机器](#大型机器) · [自动化升级](#自动化升级) · [配置](#配置) · [常见问题](#常见问题) · [兼容性](#兼容性) · [开发者](#开发者) · [更新日志](#更新日志)

</div>

---

## 这是什么

《通用机械：增强》和《通用机械：解放》都重写了通用机械的升级计算：速度和能量升级怎么换算成每 tick 操作数、每操作耗能和储能上限。两者改的是同一段方法体，所以一起安装时只有其中一套能生效，另一套被静默覆盖。

本模组把两套逻辑重排到同一个 jar 里，按顺序相乘，互不覆盖：

- 单一 jar、单一 mod id（`mekanism_empowered_unleashed`），不需要单独的 Core 前置；
- 在原版「速度 / 能量」升级之外，增加「强化速度 / 强化能量」两个升级物品；
- 本分支对应 Minecraft 1.20.1 + Forge 47.4+；1.21.1 见 [1.21.1 分支](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/tree/1.21.1)。

## 安装

| 文件 | 必需 | 说明 |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.20.1-20.1-*.jar` | ✅ | 本模组 |
| `Mekanism-1.20.1-10.4.16.*.jar` | ✅ | 通用机械本体 |
| `kotlinforforge-4.12.0-all.jar` | ✅ | Kotlin 运行时 |
| Forge 47.4+ | ✅ | 加载器 |

- 平台：Minecraft 1.20.1 + Forge 47.4+，JDK 17。
- **不要同时安装原版《通用机械：增强》或《通用机械：解放》**：功能已全部并入本模组，同装会互相覆盖。
- `-sources.jar` 与 `-api.jar` 是给开发者的，正常游玩不需要。

## 两套升级

| | 原版线 | 强化线 |
|---|---|---|
| 物品 | 速度升级、能量升级（通用机械自带） | 强化速度升级、强化能量升级（本模组新增） |
| 数量上限 | `maxUpgrades`，默认 16，可配 8–32 | `maxEmpoweredUpgrades`，默认 16，可配 1–32 |
| 何时生效 | 插上就生效 | 原版线插满之后才开始生效 |
| 效果 | 通用机械原生公式 | 在原生结果上再乘 `强化倍率^(已装/上限)` |

通用机械原生只允许 8 个速度/能量升级。本模组把上限抬到 `maxUpgrades`（默认 16），所以第 9 个开始是本模组在起作用；强化线以「插满原版线」为解锁条件。

默认配置（16 / 16 / 16 / 16）下，一台普通机器：

| 指标 | 相对原版 |
|---|---|
| 每件加工耗时 | ×1/2,000 |
| 每 tick 耗电 | ×8,944 |
| 每件加工耗电 | ×4.47 |
| 储能上限 | ×2,000 |
| 满电续航 | ×0.22 |

`maxUpgrades` 调高时，强化倍率会自动稀释：

| `maxUpgrades` | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

## 耗电怎么算

- **原版线**：`vanillaEnergyOffsetRate`，默认 1.0，即原版行为——速度升级数和能量升级数相同时，每件耗电回到原版水平。
- **强化线**：`empoweredEnergyOffsetRate`，默认 0.5，即强化速度带来的额外耗能只抵一半；默认配置下插满强化线，每件耗电约为原版的 4.47 倍。

| `empoweredEnergyOffsetRate` | 每 tick 耗电 | 每件耗电 | 满电续航 |
|---|---|---|---|
| 1.0 | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

只插速度、不插能量时耗电暴涨，这是通用机械本身的设定；储能上限单独由能量升级决定（默认插满 ×2,000）。

## 大型机器

面向《通用机械：更多机器》（Mekanism: MoreMachine）1.2.x 的六台大型机器：大型回旋式气液转换机、大型电解分离机、大型化学灌注机、大型太阳能中子活化器、大型反质子核合成器、大型颜料混合器。更早的 MoreMachine（1.1.x 及以前）没有这些机器。

### 速度

大型机器沿用 MoreMachine 自己的算法，不做换算：

```
每 tick 操作数 = 4 × 原版速度升级数 × 2^原版速度升级数
强化速度插满后：每 tick 操作数 += 4 × 原版速度升级数 × 2 × 2^强化速度升级数
```

- 16 个原版速度：4,194,304 次/tick；
- 再加 16 个强化速度：12,582,912 次/tick；
- 32 + 32 时原生公式超过 int 上限，本模组把它钳制在 10.7 亿次/tick。

### 耗电

大型机器另有一层**单价上限**（不可配置，只作用于 `com.jerry.meklm` 的大型机器）：

```
单价上限 = ⌊2,147,483,647 ÷ 输入罐容量(mB)⌋ × (1 − 抵消进度)
抵消进度 = (1.0 × 原版能量升级数 + 0.5 × 强化能量升级数) ÷ (2 × maxUpgrades)
```

机器每 tick 最多消耗一罐料，所以满罐一次跑完的总耗电不会超过 2,147,483,647 FE：

| 机器 | 输入罐 | 每操作消耗 | 双 0 抵消（满罐） | 默认抵消（满罐） |
|---|---|---|---|---|
| 大型回旋式气液转换机 | 10,000,000 mB | 1 mB 水 | 2,140,000,000 FE | 530,000,000 FE |
| 大型电解分离机 | 24,000,000 mB | 2 mB 水 | 1,068,000,000 FE | 264,000,000 FE |
| 大型化学灌注机 | 5,000,000 mB | 1 mB | 2,145,000,000 FE | 535,000,000 FE |
| 大型太阳能中子活化器 | 10,000,000 mB | 1 mB | 2,140,000,000 FE | 530,000,000 FE |
| 大型反质子核合成器 | 10,000 mB | 1 mB | 2,147,480,000 FE | 536,870,000 FE |
| 大型颜料混合器 | 1,000 mB | 1 mB | 2,147,483,000 FE | 536,870,000 FE |

（后四行按公式推算，未逐一实测；实测值见更新日志。）

大型电解分离机使用固定用量能量容器（`FixedUsageEnergyContainer`），它的单价不经过通用公式，本模组在这条路径上单独接入补偿。

## 自动化升级

| 升级 | 每台上限 | 效果 |
|---|---|---|
| 自动输入升级 | 1 | 机器多出一个「传输配置」标签页，六面默认关闭，开启后从相邻方块抽取：物品 8/t、流体 128 mB/t、化学品 128 mB/t、能量 128 FE/t |
| 快速物品输入升级 | 8 | 缩短自动输入的间隔：`21^((8−已装)/8)−1` tick，装 1 个 13 tick，插满每 tick 一次 |
| 快速物品弹出升级 | 8 | 缩短输出间隔，并提高流体/化学品的单次弹出量（插满约 ×9） |
| 输入/输出容量升级 | 8 | 流体/能量/化学品 ×(1+32×进度)，插满 ×33；物品 ×8^进度，插满 ×8 |
| 滴管（四档） | — | 手掌大小的便携容器，左键从别的容器抽、右键灌进去 |

| 滴管 | 容量 | 传输速率 | 合成 |
|---|---|---|---|
| 基础滴管 | 48,000 mB | 768 mB/t | 基础合金 + 通用机械滴管 |
| 高级滴管 | 80,000 mB | 1,280 mB/t | 灌注合金 + 基础滴管 |
| 精英滴管 | 112,000 mB | 1,792 mB/t | 强化合金 + 高级滴管 |
| 终极滴管 | 144,000 mB | 2,304 mB/t | 原子合金 + 精英滴管 |

## 配置

配置文件：`config/MekanismEmpowered Unleashed/general.toml`（服务器配置，改完需重启或 `/reload`）。

| 配置项 | 默认 | 范围 | 作用 |
|---|---|---|---|
| `maxUpgrades` | 16 | 8–32 | 原版速度/能量升级上限，同时是强化线的解锁门槛 |
| `maxEmpoweredUpgrades` | 16 | 1–32 | 强化速度/能量升级上限，只决定强化层拆几级 |
| `maxUpgradeMultiplier` | 20 | ≥1 | 强化线插满时的倍率；`maxUpgrades` 高于 16 时按 `倍率^(16/maxUpgrades)` 稀释 |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0–1.0 | 原版能量升级抵掉多少提速耗能 |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0–1.0 | 强化能量升级抵掉多少提速耗能 |
| `enchantableMekaGear` | false | — | 允许 Meka 装备附魔 |
| `maxOperationsPerTick` | 128 | 1–10000 | 泵、数字型采矿机、流体装填器、公式装配器每 tick 的补次上限 |
| `autoScaleOperationsPerTick` | true | — | 上一项跟随 `maxUpgrades` 自动抬高 |

滴管数值在 `config/MekanismEmpowered Unleashed/tiers.toml`。

## 常见问题

**插了强化速度没有反应。** 强化线要求原版速度升级插满（默认 16 个）。

**插满后机器反而变慢 / 停机。** 每 tick 耗电超过储能，机器会降速甚至停在「能量不足」。加能量升级或加储能。

**大型机器能耗显示 G 级。** 大型机器沿用 MoreMachine 的原生速度算法，单 tick 操作数本来就上百万；单价上限把它压在 2,147,483,647 FE 以内。

**泵、数字型采矿机插满速度没有更快。** 这四台机器有每 tick 补次上限，默认 128 次。

**能不能只装一个版本？** 不能。本模组同时包含两套逻辑，原版《增强》《解放》都不要装。

## 兼容性

| 对象 | 状态 |
|---|---|
| 通用机械 10.4.16.x | 必需 |
| 通用机械：发电机 / 附加 / 工具 | 支持，与机器共用同一套公式 |
| 更多机器（MoreMachine）1.2.x 的大型机器 | 单独适配（见上文） |
| 通用机械：扩展（Extras） / Evolved Mekanism | 可选注入，目标类缺失时跳过 |
| 无用之物（UselessMod） | 可共存 |
| 原版《增强》/《解放》 | **不要同装** |

卸载前请先把机器里的强化升级取出来；卸载后这些物品会变成未知物品。

## 开发者

### 实现方式

1.20.1 分支与 1.21.1 结构一致：`mixin/common` 下的 Java mixin 用 `@ModifyExpressionValue` / `@Inject` 定点注入，实际计算放在 `mixin_impl/*.kt`；`mixin/unleashed` 下的 Kotlin mixin 直接挂在 Mekanism 的工具类上。

| 注入点 | 文件 | 作用 |
|---|---|---|
| `MekanismUtils.getTicksD` / `getOperationsPerTick` / `getEnergyPerTick` / `getMaxEnergy` | `mixin/unleashed/MixinMekanismUtils.kt` | HEAD 接管，换成带强化层的公式 |
| `TileEntityMekanism.recalculateUpgrades` 的速度分支 | `mixin/common/tile/MixinModifyRecalculationTarget.java` | 让强化速度也触发原版速度重算 |
| 大型机器 `recalculateUpgrades` 的 `Math.pow(2, count)` | `mixin/common/tile/machine/MixinModifyRecalculationBaselineMaxOperations.java` | 改写 MoreMachine 的 baseline |
| `MachineEnergyContainer.updateEnergyPerTick` / `updateMaxEnergy` | `mixin/common/MixinMachineEnergyContainer.java` | 让强化线参与重算，并把每 tick 能耗钳到储能 |
| `FixedUsageEnergyContainer.updateEnergyPerTick` | `mixin/common/MixinFixedUsageEnergyContainer.java` | 覆盖电解分离机这条路径 |
| `Upgrade.getMax` | `mixin/unleashed/MixinUpgradeMax.kt` | 把 8 个的上限换成配置值 |

1.20.1 的能耗容器用 `FloatingLong`（1.21.1 是 `long`），补偿与钳制都按 `FloatingLong` 实现（`FloatingLong.create(...)` + `greaterThan`）。

### 构建

需要 JDK 17 与可访问 Maven 仓库的网络：

```
gradlew build
```

产物在 `build/libs/`：`-<version>.jar` 是发行包，`-api.jar` 与 `-sources.jar` 供开发者引用。

## 更新日志

### 20.1-1.0.4

**大型机器的速度恢复原生语义**

- 每 tick 操作数恢复为 MoreMachine 原生算法：`4 × 原版速度数 × 2^原版速度数`，装了强化速度再叠加 `4 × 原版速度数 × 2 × 2^强化速度数`。
- 原因：20.1-1.0.2 把大型机器换成普通机器的换算口径（`倍率^(总速度/8)`），16 个原版速度升级从 4,194,304 次/tick 掉到 448 次/tick；20.1-1.0.3 让这个换算无条件生效，于是大型回旋式气液转换机的吞吐退到和小型机相当。
- 32 + 32 时原生公式超过 int 上限，本模组把它钳制在 1,073,741,696 次/tick（保留整数余量，不越界、不为负）。

**大型机器的能耗上限**

- 新增单价上限：`⌊2,147,483,647 ÷ 输入罐容量⌋ × (1 − 抵消进度)`，抵消进度 = `(1.0 × 原版能量数 + 0.5 × 强化能量数) ÷ (2 × maxUpgrades)`。
- 机器每 tick 最多消耗一罐料，因此满罐一次跑完的总耗电不超过 2,147,483,647 FE；双 0 抵消时按罐容量定满（回旋 10,000,000 × 214 = 2,140,000,000 FE），默认 1.0/0.5 时为 1/4。
- 该上限对 `com.jerry.meklm.*` 生效，不可配置，也不改变普通机器的公式。

**大型电解分离机接入补偿**

- 它使用 `FixedUsageEnergyContainer`：`updateEnergyPerTick()` 直接写入单价，不经过 `MekanismUtils.getEnergyPerTick()`，此前的补偿与钳制对它完全无效。
- 修复前实测：400 FE/次，一罐水（24,000,000 mB）消耗 3,000,000,000 FE。
- 修复后：双 0 时为 89 FE/次，默认 1.0/0.5 时为 22 FE/次。

**保留项**

- 整数保护与「每 tick 能耗不超过储能」的钳制不变。
- 普通机器不参与补偿。

<details>
<summary><b>20.1-1.0.3（已被 20.1-1.0.4 回退）</b></summary>

- 大型机器改用普通机器口径 `倍率^(总速度/8)`：16 个原版速度的操作数由 4,194,304 次/tick 降到约 128 次/tick。
- 换算改为无条件生效，修掉 20.1-1.0.2「插了强化速度反而更慢」的回归。
- **回退原因**：压操作数让大型机器比原生设计慢得多，20.1-1.0.4 恢复 MoreMachine 的原生算法，改为在能耗侧封顶。

</details>

<details>
<summary><b>20.1-1.0.2（部分改动已被 20.1-1.0.4 回退）</b></summary>

- 修复大型机器插满升级后停机：MoreMachine 把每 tick 操作数算成 `baseOperations × 2^原版速度数` 的整数乘积，速度高时溢出成负数，`储能 ÷ 能耗` 得到 0，机器同时挂上「能量不足」和「降速」并停机。
- 追加「每 tick 能耗不超过储能」的钳制；最坏情况是降速而不是停机。
- 把电解分离机使用的 `FixedUsageEnergyContainer` 纳入钳制。
- **回退原因**：本版改写的速度换算在 20.1-1.0.4 中被回退，钳制部分保留。

</details>

<details>
<summary><b>20.1-1.0.1</b></summary>

- 修复强化速度写不进模组自己的 NBT 分区（区间差一位，第一个追加的升级一直留在原版 `upgrades` 列表里）。
- 老存档自动迁移：读档时从原版列表捡回数量再清理列表；没有分区的存档不再被清理。
- 修复 1.20.1 专属的可选注入目标（`com.jerry.mekextras.*` → `com.jerry.mekanism_extras.*`，以及 mekaf / mekmm 基类）。
- 1.20.1 专用服务器实测：16 原版速度/能量 + 8 强化速度/能量 重启后数量不变。

</details>

<details>
<summary><b>20.1-1.0.0</b></summary>

- 首个 1.20.1 版本：Forge 47 + Kotlin for Forge 4.x，单一 jar。
- 两套升级体系（上限可配 8–32，默认 16）、六种新升级、四档滴管。
- 泵、数字型采矿机、流体装填器、公式装配器支持每 tick 多次操作。
- 修复强化升级重载后从 16 掉到 8（1.20.1 直接读 `maxStack` 字段，绕过上限改写）。
- 修复升级必须按顺序插入才生效；修复放置方块恢复配置 NBT 失败（core source set 的 refmap 没打进 jar）。
- 修复创造模式页签标题显示英文；兼容性加固与 tooltip 修正。

</details>

## 许可证与署名

- 基于 [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered)（Copyright © 2025 Lapis，MIT）与 Mekanism Unleashed（Copyright © 2024 WhitePhant0m，MIT）
- 合并与修改：Copyright © 2026 zaixiayesheng，MIT License
- 完整协议见 [LICENSE](./LICENSE)

## 反馈

Bug 与建议请到 [GitHub Issues](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)。

提交前请附上：Minecraft 与 Forge 版本、Mekanism 与 MoreMachine 版本、本模组版本、`logs/latest.log`，以及涉及的机器与升级插法。
