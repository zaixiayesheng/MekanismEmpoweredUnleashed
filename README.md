<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">通用机械：增强·解放</h1>

<h3 align="center">Mekanism: Empowered Unleashed</h3>

<div align="center">

通用机械（Mekanism）的升级扩展：把《通用机械：增强》与《通用机械：解放》合并成一个模组，在原版升级之上再加一层「强化」升级。

English version: [README_EN.md](./README_EN.md)

[![Release](https://img.shields.io/github/v/release/zaixiayesheng/MekanismEmpoweredUnleashed?filter=v21.1*&style=flat-square&label=Release&color=blue)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/releases)
[![CurseForge](http://cf.way2muchnoise.eu/1702076.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green.svg?style=flat-square)](https://www.minecraft.net)
[![NeoForge 21.1](https://img.shields.io/badge/NeoForge-21.1-orange.svg?style=flat-square)](https://neoforged.net)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](./LICENSE)

</div>

<div align="center">

[这是什么](#这是什么) · [安装](#安装) · [两套升级](#两套升级) · [耗电怎么算](#耗电怎么算) · [大型机器](#大型机器) · [自动化升级](#自动化升级) · [配置](#配置) · [常见问题](#常见问题) · [兼容性](#兼容性) · [开发者](#开发者) · [更新日志](#更新日志)

</div>

---

## 这是什么

《通用机械：增强》和《通用机械：解放》都重写了通用机械的升级计算：速度和能量升级怎么换算成每 tick 操作数、每操作耗能和储能上限。两者改的是同一段方法体，所以一起安装时只有其中一套能生效，另一套被静默覆盖——表现就是某些升级失效、或者速度对不上。

本模组把两套逻辑重排到同一个 jar 里，按顺序相乘，互不覆盖：

- 单一 jar、单一 mod id（`mekanism_empowered_unleashed`），不需要单独的 Core 前置；
- 在原版「速度 / 能量」升级之外，增加「强化速度 / 强化能量」两个升级物品；
- 1.21.1（NeoForge 21.1+）与 1.20.1（Forge 47+）两个版本线并行维护。

## 安装

### 1.21.1（本文件对应版本）

| 文件 | 必需 | 说明 |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.21.1-21.1-*.jar` | ✅ | 本模组 |
| `Mekanism-1.21.1-10.7.19.*.jar` | ✅ | 通用机械本体 |
| `kotlinforforge-5.12.0-all.jar` | ✅ | Kotlin 运行时 |
| NeoForge 21.1.x | ✅ | 加载器 |

- 平台：Minecraft 1.21.1 + NeoForge 21.1.x，JDK 21。
- **不要同时安装原版《通用机械：增强》或《通用机械：解放》**：功能已全部并入本模组，同装会互相覆盖。
- `-sources.jar` 与 `-api.jar` 是给开发者的，正常游玩不需要。

### 1.20.1

见 [1.20.1 分支](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/tree/1.20.1)：Minecraft 1.20.1 + Forge 47.4+ + Kotlin for Forge 4.12，JDK 17，通用机械 10.4.16.x。

## 两套升级

| | 原版线 | 强化线 |
|---|---|---|
| 物品 | 速度升级、能量升级（通用机械自带） | 强化速度升级、强化能量升级（本模组新增） |
| 数量上限 | `maxUpgrades`，默认 16，可配 8–32 | `maxEmpoweredUpgrades`，默认 16，可配 1–32 |
| 何时生效 | 插上就生效 | 原版线插满之后才开始生效 |
| 效果 | 通用机械原生公式（每 tick 操作数、每操作耗能、储能） | 在原生结果上再乘 `强化倍率^(已装/上限)` |

通用机械原生只允许 8 个速度/能量升级。本模组把上限抬到 `maxUpgrades`（默认 16），所以第 9 个开始是本模组在起作用；强化线以「插满原版线」为解锁条件。

默认配置（16 / 16 / 16 / 16，即原版速度、原版能量、强化速度、强化能量各 16）下，一台普通机器：

| 指标 | 相对原版 |
|---|---|
| 每件加工耗时 | ×1/2,000 |
| 每 tick 耗电 | ×8,944 |
| 每件加工耗电 | ×4.47 |
| 储能上限 | ×2,000 |
| 满电续航 | ×0.22 |

`maxUpgrades` 调高时，强化倍率会自动稀释，避免速度和储能一起失控：

| `maxUpgrades` | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

`maxEmpoweredUpgrades` 不改变合计，只决定强化层拆成几级（16 级时每级约 +20.6%，32 级时约 +9.7%）。

## 耗电怎么算

速度升级提高每 tick 的操作数，能量升级降低每操作的耗能，两者相抵的比例就是抵消率。

- **原版线**：`vanillaEnergyOffsetRate`，默认 1.0，即原版行为——速度升级数和能量升级数相同时，每件耗电回到原版水平。可配 0.0–1.0。
- **强化线**：`empoweredEnergyOffsetRate`，默认 0.5，即强化速度带来的额外耗能只抵一半。默认配置下插满强化线，每件耗电约为原版的 4.47 倍；填 1.0 则回到原版水平，填 0.0 则完全不做抵消。

| `empoweredEnergyOffsetRate` | 每 tick 耗电 | 每件耗电 | 满电续航 |
|---|---|---|---|
| 1.0 | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

只插速度、不插能量时耗电暴涨，这是通用机械本身的设定；储能上限单独由能量升级决定（默认插满 ×2,000）。

## 大型机器

面向《通用机械：更多机器》（Mekanism: MoreMachine）的六台大型机器：大型回旋式气液转换机、大型电解分离机、大型化学灌注机、大型太阳能中子活化器、大型反质子核合成器、大型颜料混合器。

### 速度

大型机器沿用 MoreMachine 自己的算法，不做换算：

```
每 tick 操作数 = 4 × 原版速度升级数 × 2^原版速度升级数
强化速度插满后：每 tick 操作数 += 4 × 原版速度升级数 × 2 × 2^强化速度升级数
```

- 16 个原版速度：4,194,304 次/tick；
- 再加 16 个强化速度：12,582,912 次/tick；
- 32 + 32 时原式约为 42.9 亿，会在 int 上溢出，本模组把它钳制在 10.7 亿次/tick。

### 耗电

按上面的次数付电，一罐料几百毫秒就烧掉整条能量，所以大型机器另有一层**单价上限**（不可配置，只作用于 `com.jerry.meklm` 的大型机器）：

```
单价上限 = ⌊2,147,483,647 ÷ 输入罐容量(mB)⌋ × (1 − 抵消进度)
抵消进度 = (1.0 × 原版能量升级数 + 0.5 × 强化能量升级数) ÷ (2 × maxUpgrades)
```

因为机器每 tick 最多消耗一罐料，所以「满罐一次跑完」的总耗电不会超过 2,147,483,647 FE：

| 机器 | 输入罐 | 每操作消耗 | 双 0 抵消（满罐） | 默认抵消（满罐） |
|---|---|---|---|---|
| 大型回旋式气液转换机 | 10,000,000 mB | 1 mB 水 | 2,140,000,000 FE | 530,000,000 FE |
| 大型电解分离机 | 24,000,000 mB | 2 mB 水 | 1,068,000,000 FE | 264,000,000 FE |
| 大型化学灌注机 | 5,000,000 mB | 1 mB | 2,145,000,000 FE | 535,000,000 FE |
| 大型太阳能中子活化器 | 10,000,000 mB | 1 mB | 2,140,000,000 FE | 530,000,000 FE |
| 大型反质子核合成器 | 10,000 mB | 1 mB | 2,147,480,000 FE | 536,870,000 FE |
| 大型颜料混合器 | 1,000 mB | 1 mB | 2,147,483,000 FE | 536,870,000 FE |

（后四行按公式推算，未逐一实测；实测值见更新日志。）

大型电解分离机使用固定用量能量容器（`FixedUsageEnergyContainer`），它的单价不经过通用公式，本模组在这条路径上单独接入补偿，否则它拿不到上面的上限。

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

滴管数值在 `config/MekanismEmpowered Unleashed/tiers.toml`：`basicCapacity` / `basicRate` … `ultimateCapacity` / `ultimateRate`。

## 常见问题

**插了强化速度没有反应。** 强化线要求原版速度升级插满（默认 16 个），没插满时强化速度不生效。

**插满后机器反而变慢 / 停机。** 每 tick 耗电超过储能，机器会降速甚至停在「能量不足」。加能量升级或强化能量升级，或加储能。

**大型机器能耗显示 G 级。** 大型机器沿用 MoreMachine 的原生速度算法，单 tick 操作数本来就上百万；上面的单价上限把它压在 2,147,483,647 FE 以内，插满时贴着上限跑是正常的。

**泵、数字型采矿机插满速度没有更快。** 这四台机器有每 tick 补次上限，默认 128 次；调高 `maxOperationsPerTick` 或让 `autoScaleOperationsPerTick` 跟随 `maxUpgrades`。

**能不能只装一个版本？** 不能。本模组同时包含两套逻辑，原版《增强》《解放》都不要装。

## 兼容性

| 对象 | 状态 |
|---|---|
| 通用机械 10.7.19.x | 必需 |
| 通用机械：发电机 / 附加 / 工具 | 支持，与机器共用同一套公式 |
| 更多机器（MoreMachine）的大型机器 | 单独适配（见上文） |
| 通用机械：扩展（Extras） / Evolved Mekanism | 可选注入，目标类缺失时跳过 |
| 无用之物（UselessMod） | 可共存（1.0.4 起移除了不兼容声明） |
| 原版《增强》/《解放》 | **不要同装** |

本模组不修改存档格式，也不添加方块；卸载后已有存档里的强化升级物品会变成「未知物品」并丢失，卸载前请先把机器里的强化升级取出来。

## 开发者

### 实现方式

| 注入点 | 文件 | 作用 |
|---|---|---|
| `MekanismUtils.getTicksD` / `getOperationsPerTick` / `getEnergyPerTick` / `getMaxEnergy` | `mixin/unleashed/MixinMekanismUtils.kt` | HEAD 接管，换成带强化层的公式 |
| `TileEntityMekanism.recalculateUpgrades` 的速度分支 | `mixin/common/tile/MixinModifyRecalculationTarget.java` | 让强化速度也触发原版速度的重算 |
| 大型机器 `recalculateUpgrades` 的 `Math.pow(2, count)` | `mixin/common/tile/machine/MixinModifyRecalculationBaselineMaxOperations.java` | 改写 MoreMachine 的 baseline |
| `MachineEnergyContainer.updateEnergyPerTick` / `updateMaxEnergy` | `mixin/common/MixinMachineEnergyContainer.java` | 让强化线参与重算，并把每 tick 能耗钳到储能 |
| `FixedUsageEnergyContainer.updateEnergyPerTick` | `mixin/common/MixinFixedUsageEnergyContainer.java` | 同上，覆盖电解分离机这条路径 |
| `Upgrade.getMax` | `mixin/unleashed/MixinUpgradeMax.kt` | 把 8 个的上限换成配置值 |

源码分三层：`src/main/kotlin`（模组本体与 mixin 实现）、`src/core`（升级系统与前置接口）、`src/data` + `src/generated`（配方与语言文件的数据生成）。

### 构建

需要 JDK 21 与可访问 Maven 仓库的网络：

```
gradlew build
```

产物在 `build/libs/`：`-<version>.jar` 是发行包，`-api.jar` 与 `-sources.jar` 供开发者引用。

## 更新日志

### 21.1-1.0.10

**大型机器的速度恢复原生语义**

- 每 tick 操作数恢复为 MoreMachine 原生算法：`4 × 原版速度数 × 2^原版速度数`，装了强化速度再叠加 `4 × 原版速度数 × 2 × 2^强化速度数`。
- 原因：1.0.8 把大型机器换成普通机器的换算口径（`倍率^(总速度/8)`），16 个原版速度升级从 4,194,304 次/tick 掉到 448 次/tick；1.0.9 让这个换算无条件生效，于是「大型回旋式气液转换机」的吞吐退到和小型机相当。
- 32 + 32 时原生公式约为 42.9 亿，超过 int 上限；本模组把它钳制在 10.7 亿次/tick 以内（32 + 32 实测为 1,073,741,696 次/tick，保留整数余量，不越界、不为负）。

**大型机器的能耗上限**

- 新增单价上限：`⌊2,147,483,647 ÷ 输入罐容量⌋ × (1 − 抵消进度)`，抵消进度 = `(1.0 × 原版能量数 + 0.5 × 强化能量数) ÷ (2 × maxUpgrades)`。
- 机器每 tick 最多消耗一罐料，因此满罐一次跑完的总耗电不超过 2,147,483,647 FE；双 0 抵消时按罐容量定满（回旋 10,000,000 × 214 = 2,140,000,000 FE），默认 1.0/0.5 时为 1/4。
- 该上限对 `com.jerry.meklm.*` 生效，不可配置，也不改变普通机器的公式。

**大型电解分离机接入补偿**

- 它使用 `FixedUsageEnergyContainer`：`updateEnergyPerTick()` 直接写入单价，不经过 `MekanismUtils.getEnergyPerTick()`，此前的补偿与钳制对它完全无效。
- 修复前实测：400 FE/次，一罐水（24,000,000 mB）消耗 3,000,000,000 FE。
- 修复后：该路径的返回值同样经过单价上限与储能钳制；双 0 时为 89 FE/次，默认 1.0/0.5 时为 22 FE/次。

**保留项**

- 整数保护（`Int.MAX_VALUE` 余量）与「每 tick 能耗不超过储能」的钳制不变。
- 普通机器不参与补偿：默认配置下满配普通机器每 tick 耗电约占储能的 11%，加补偿反而会让高配近乎免费。

<details>
<summary><b>21.1-1.0.9（已被 1.0.10 回退）</b></summary>

- 大型机器改用普通机器口径 `倍率^(总速度/8)`：16 个原版速度的操作数由 4,194,304 次/tick 降到约 128 次/tick，能量栏不再出现与储能同量级的数字（例如 2.14G FE/t）。
- 换算改为无条件生效，修掉了 1.0.8「插了强化速度反而更慢」的回归。
- **回退原因**：压操作数让大型机器比原生设计慢得多，1.0.10 恢复 MoreMachine 的原生算法，改为在能耗侧封顶。

</details>

<details>
<summary><b>21.1-1.0.8（部分改动已被 1.0.10 回退）</b></summary>

- 修复大型机器插满升级后停机：MoreMachine 把每 tick 操作数算成 `baseOperations × 2^原版速度数` 的整数乘积，速度高时溢出成负数，`储能 ÷ 能耗` 得到 0，机器同时挂上「能量不足」和「降速」并停机。
- 追加「每 tick 能耗不超过储能」的钳制；最坏情况是降速而不是停机。
- 把电解分离机使用的 `FixedUsageEnergyContainer` 纳入钳制。
- **回退原因**：本版改写的速度换算在 1.0.9/1.0.10 中被替换或回退，钳制部分保留至今。

</details>

<details>
<summary><b>21.1-1.0.7</b></summary>

- 修复强化速度写不进模组自己的 NBT 分区（区间差一位，第一个追加的升级一直留在原版 `upgrades` 列表里）。
- 老存档自动迁移：读档时从原版列表捡回数量再清理列表；没有分区的存档不再被清理。

</details>

<details>
<summary><b>21.1-1.0.6</b></summary>

- 修复自动输入升级的侧面开关网络包没有权限校验。
- 修复数字型采矿机的补采不消耗能量与延迟。
- 修复 `Temp.inject` 缺异常保护，一次异常会让四台机器的补次永久退化为每 tick 一次。
- 修复配置保存异步导致改动丢失；侧配置按钮与物品形态 tooltip 不刷新。
- 修正 I/O 容量升级的 tooltip（实际为 ×33 / ×8）。
- 兼容性加固：可选模组的 `@Pseudo` 注入不再因目标字节码不匹配而崩溃。

</details>

<details>
<summary><b>21.1-1.0.5</b></summary>

- 新增 `maxEmpoweredUpgrades`，强化线上限与 `maxUpgrades` 解耦。
- 强化倍率随 `maxUpgrades` 自动稀释（`倍率^(16/maxUpgrades)`）。
- 修正强化能量容量多乘一个平方（满配从 ×40,000 改为 ×2,000）。
- 新增 `maxOperationsPerTick` 与 `autoScaleOperationsPerTick`。
- 新增 `vanillaEnergyOffsetRate` 与 `empoweredEnergyOffsetRate`。
- 修复 Meka 装备附魔开关对装甲无效；修正强化升级 tooltip 的倍率显示。

</details>

<details>
<summary><b>21.1-1.0.4</b></summary>

- 移除与无用之物的不兼容声明，两者可共存。

</details>

<details>
<summary><b>21.1-1.0.3</b></summary>

- 移除内嵌的旧 id 桥接，主 jar 恢复单一 mod id。

</details>

<details>
<summary><b>21.1-1.0.2</b></summary>

- 修复资源蜜蜂：创世把本模组误判为不支持的依赖。

</details>

<details>
<summary><b>21.1-1.0.1</b></summary>

- 修复强化升级必须按顺序插入才生效。
- 修复泵、流体装填器、公式装配器、数字型采矿机插满后仍每 tick 只执行一次（上限 128）。
- 修复 MekaJade 显示强化升级异常。

</details>

<details>
<summary><b>21.1-1.0.0</b></summary>

- 首次发布：合并《通用机械：增强》与《通用机械：解放》，单一 jar、两套升级体系、六种新升级、四档滴管。

</details>

## 许可证与署名

- 基于 [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered)（Copyright © 2025 Lapis，MIT）与 Mekanism Unleashed（Copyright © 2024 WhitePhant0m，MIT）
- 合并与修改：Copyright © 2026 zaixiayesheng，MIT License
- 完整协议见 [LICENSE](./LICENSE)

## 反馈

Bug 与建议请到 [GitHub Issues](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)。

提交前请附上：Minecraft 与加载器版本、Mekanism 与 MoreMachine 版本、本模组版本、`logs/latest.log`，以及涉及的机器与升级插法。
