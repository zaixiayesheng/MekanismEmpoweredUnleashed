<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">通用机械：增强·解放</h1>

<h3 align="center">Mekanism: Empowered Unleashed</h3>

<div align="center">

通用机械（Mekanism）增强模组，由 **Mekanism: Empowered** 与 **Mekanism Unleashed** 合并而成。

English version: [README_EN.md](./README_EN.md)

[![Release](https://img.shields.io/github/v/release/zaixiayesheng/MekanismEmpoweredUnleashed?style=flat-square&label=Release&color=blue)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/releases)
[![CurseForge Downloads](http://cf.way2muchnoise.eu/1702076.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![CurseForge Versions](http://cf.way2muchnoise.eu/versions/1702076_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-green.svg?style=flat-square)](https://www.minecraft.net)
[![Forge 47.4](https://img.shields.io/badge/Forge-47.4-orange.svg?style=flat-square)](https://files.minecraftforge.net)
![Kotlin 2.2.21](https://img.shields.io/badge/Kotlin-2.2.21-7F52FF.svg?style=flat-square&logo=kotlin&logoColor=white)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](./LICENSE)
[![Issues](https://img.shields.io/badge/Issues-GitHub-black.svg?style=flat-square&logo=github)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)

</div>

<div align="center">

[这是什么](#-这是什么) · [特性](#-特性) · [安装](#-安装) · [兼容性](#-兼容性) · [配置](#-配置) · [升级体系与数值](#-升级体系与数值) · [与上游的差异](#-与上游的差异) · [已知问题](#-已知问题) · [构建](#-构建) · [更新日志](#-更新日志) · [许可证与署名](#-许可证与署名) · [反馈](#-反馈)

</div>

---

## 📖 这是什么

两个模组装在一起会冲突：它们都在用 Mixin 改 Mekanism 里计算升级效果的 `MekanismUtils`。Unleashed 用 `@Overwrite` 整段替换方法体，Empowered 用定点注入；前者一生效，原版的指令就没了，后者的注入要么在启动时崩溃，要么静默失效。

这个项目把两者合成一个模组：

- 单一 jar、单一 mod id（`mekanism_empowered_unleashed`），Empowered 原来那个独立的 Core 前置也一并并入本体
- 全部代码统一为 Kotlin
- 注入点重新排开，两层效果按顺序相乘，不再互相覆盖

本分支面向 Minecraft 1.20.1 + Forge 47.4.0，只兼容原版 Mekanism（1.20.1 / 10.4.16.x）。

## ✨ 特性

- **16+16 双层升级体系**：原版速度/能量升级（第一层，默认上限 16）+ 强化速度/能量升级（第二层，第一层插满后解锁）
- **两层上限分开配**：`maxUpgrades`（8–32，默认 16）是原版线上限，也是强化线的解锁门槛；`maxEmpoweredUpgrades`（1–32，默认 16）单管强化线，只决定强化层的总加成拆成几级
- **强化倍率自动稀释**：`maxUpgrades` 调高时 `maxUpgradeMultiplier` 按 `倍率^(16/maxUpgrades)` 自动变小（上限 32 时 20 → 4.47），不会因为调高原版上限就速度和储能一起失控
- **修掉减耗封顶**：原来的能量公式把能量升级的效果封顶在 8，插 32 个和插 8 个耗电一样；现在封顶值跟着 `maxUpgrades` 走
- **四台机器支持每 tick 多操作**：电动泵、数字型采矿机、流体装填器、公式装配器（速度超过 1 tick/操作后继续加速，上限可配）
- **自动输入与容量升级**：快速物品输入/弹出、输入输出容量、自动输入升级（自带窗口位置记忆与速率配置）
- **四级滴管**：基础/高级/精英/终极滴管，速率与容量写进 `tiers.toml`
- **Meka 装备附魔开关**（`enchantableMekaGear`，继承自 Unleashed，默认关闭）
- **上游附属模组联动**：Mekanism Extras、Evolved Mekanism、Mekanism Elements、More Machine、Evolved Extras 的机器一并纳入强化升级与补次的作用范围

## 📦 安装

| 文件 | 必需 | 说明 |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.20.1-20.1-1.0.0.jar` | ✅ | 本模组，只需要这一个文件 |
| `kotlinforforge-4.12.0-all.jar` | ✅ | Kotlin 运行时前置，**缺失会直接拒绝加载** |
| Mekanism 1.20.1-10.4.16.80（10.4 线） | ✅ | 本体依赖 |

- 平台：Minecraft 1.20.1 + Forge 47.4.0+
- **Kotlin for Forge 是硬前置**：本模组唯一一个缺失就会在加载阶段被拒绝的依赖，装之前先确认它在 `mods` 里
- **不要与原版同时安装**：合并版已包含 `mekanism-empowered`（含其 Core）和 `mekanism-unleashed` 的全部功能
- `-sources.jar`、`-api.jar` 为开发者配套，无需安装

## 🤝 兼容性

### 可选模组

| 模组 | 最低版本 |
|---|---|
| Mekanism Extras | 1.5.0 |
| Evolved Mekanism | 1.2.1 |
| Mekanism Elements | 2.3 |
| More Machine | 1.2.1 |
| Evolved Extras | 1.4.1 |

这些是可选依赖，缺任何一个都不影响启动。装上之后，它们新增的机器会一起纳入强化升级与每 tick 补次的作用范围。

### 与无用之物（UselessMod）共存

无用之物也覆写了 `UpgradeUtils` 的两个升级叠加提示方法，和本模组做的是同一件事。本模组给这两个方法设了更高的 mixin 优先级（2000），两边都在时由本模组生效，所以可以一起装。

### 声明不兼容

元数据里声明了不兼容，装上会直接拒绝启动：

- `mekanism_unleashed`：功能已并入本模组
- `mekanismtweaks`、`mekanismupgradesreborn`：改的是同一批升级逻辑

## ⚙️ 配置

配置文件：`config/MekanismEmpowered Unleashed/general.toml`（升级与机器）、`tiers.toml`（滴管数值）。

| 配置项 | 默认值 | 范围 | 作用 |
|---|---|---|---|
| `maxUpgrades` | 16 | 8–32 | 原版速度/能量升级的上限，也是强化升级的解锁门槛（插满这么多个才解锁） |
| `maxEmpoweredUpgrades` | 16 | 1–32 | 强化速度/能量升级的上限。只决定强化层的总加成拆成几级，插满时的总加成由 `maxUpgradeMultiplier` 决定 |
| `maxUpgradeMultiplier` | 20 | ≥1 | 强化线插满时的效果倍率。`maxUpgrades` 高于 16 时自动稀释为 `倍率^(16/maxUpgrades)`（上限 24 → 7.37、上限 32 → 4.47） |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0–1.0 | 原版能量升级抵掉多少额外耗能。1.0 表示速度与能量数量相同时，每操作总耗电回到原版 |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0–1.0 | 强化能量升级的抵消率。默认只抵一半，所以强化层不是能量中性的；填 1.0 恢复每操作总能耗 ×1 |
| `enchantableMekaGear` | false | — | 原版 Meka 装备不能附魔；开启后允许，但要自己把物品加进可附魔标签 |
| `maxOperationsPerTick` | 128 | 1–10000 | 泵、数字型采矿机、流体装填器、公式装配器每 tick 的补次上限。速度超过这个量级，对这四台机器不再有可见效果；调高会加重服务器负担 |
| `autoScaleOperationsPerTick` | true | — | 上一项是否跟着 `maxUpgrades` 抬高（只升不降）。默认开启，只调 `maxUpgrades` 就够；关掉后它才是硬上限 |

滴管的数值单独放在 `tiers.toml`：

| 滴管 | 默认速率 | 默认容量 |
|---|---|---|
| 基础滴管 | 768 mB/t | 48,000 mB |
| 高级滴管 | 1,280 mB/t | 80,000 mB |
| 精英滴管 | 1,792 mB/t | 112,000 mB |
| 终极滴管 | 2,304 mB/t | 144,000 mB |

## 📊 升级体系与数值

以默认配置（`maxUpgrades=16`、`maxEmpoweredUpgrades=16`、原版倍率 10、强化倍率 20）为例，16 速度 + 16 能量 + 16 强化速度 + 16 强化能量全满时：

| 指标 | 倍率 |
|---|---|
| 每操作耗时 | ×1/2000 |
| 每 tick 能耗 | ×8,944 |
| **每操作总能耗** | **×4.47** |
| 能量存储 | ×2,000 |
| **满电续航** | **×0.22** |
| 加工速度 | ×2000 |

原版线按 `10^(N/8)` 涨，强化线按 `20^(16/N)` 稀释，所以总倍率随 `maxUpgrades` 单调上升，但不会失控：

| maxUpgrades | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

`maxEmpoweredUpgrades` 不改变上表的合计，只改变强化层被拆成几级（16 级时每级约 +20%、32 级时每级约 +10%）。

> 旧版把两层上限绑在一个旋钮上，`maxUpgrades=32` 时合计冲到 ×200,000、储能也跟着涨 20 万倍，很容易顶到 `Long.MAX`；现在 32 档是 ×44,700。

### 耗电怎么算

速度升级让机器每 tick 耗电变多，能量升级让它变少，两者相抵的比例就是抵消率。原版线默认 100% 抵消（`vanillaEnergyOffsetRate = 1.0`），速度数和能量数相同时，每加工一件物品的总耗电回到原版；强化线默认只抵一半（`empoweredEnergyOffsetRate = 0.5`），插满后每操作耗电高于原版。只插速度不插能量，耗电会暴涨，这是原版就有的设定。

| `empoweredEnergyOffsetRate` | 每 tick 能耗 | 每操作总能耗 | 满电续航 |
|---|---|---|---|
| 1.0 | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

### 容量为什么是 ×2,000

容量的强化系数和速度、每 tick 能耗同阶：容量倍率 = 原版线 ×100 × 强化层 ×20 = ×2,000。默认抵消率下每 tick 能耗是 ×8,944，所以满电续航降到 ×0.22（抵消率填 1.0 时是 ×1）。升级放大的是功率，想更耐跑得另加容量升级。

### 四台机器另有一条上限

电动泵、数字型采矿机、流体装填器、公式装配器各有一个每 tick 补次循环，受 `maxOperationsPerTick`（默认 128）约束。速度升到万级倍率后，理论需求远超这条线，这就是「插满速度升级却看不出更快」的原因。`autoScaleOperationsPerTick = true` 时这条线跟着 `maxUpgrades` 抬高（原版倍率^(N/8)：16 → 128、24 → 1000、32 → 10000），只调 `maxUpgrades` 就能让四台机器一起变快；关掉开关，它就是硬上限，也可以直接调 `maxOperationsPerTick`。

## 🔧 与上游的差异

| 问题 | 处理 |
|---|---|
| Unleashed 用 `@Overwrite` 整段接管 `MekanismUtils`，Empowered 的定点注入失效 | 中枢改为定点注入，注入点错开，不再整段替换 |
| 能量升级超过 8 个不减耗 | 封顶值跟随 `maxUpgrades` 配置 |
| 强化能量升级的容量指数多乘了一个 2 | 与速度、每 tick 能耗同阶，满配容量 ×40,000 → ×2,000 |
| 四台机器补次上限硬编码 128 | 改为配置项 `maxOperationsPerTick`，默认自动跟随 `maxUpgrades` |
| 强化升级的效果提示按「每级都是满级加成」显示，比实现夸大 8 倍 | 改为显示 `强化倍率^(已装/上限)`，泵与输入输出容量两处都换成真实公式 |
| 强化能量升级的公式硬编码 8 | 按配置上限归一；抵消比例由 `empoweredEnergyOffsetRate` 决定 |
| 强化倍率不随原版上限变化 | 自动稀释成 `maxUpgradeMultiplier^(16/maxUpgrades)`，只压不抬 |
| 两层上限被一个旋钮绑死 | 拆成 `maxUpgrades`（原版线 + 解锁门槛）与 `maxEmpoweredUpgrades`（强化线） |
| 能量抵消比例写死在公式里，整合包没法调难度 | 新增 `vanillaEnergyOffsetRate`（1.0）与 `empoweredEnergyOffsetRate`（0.5） |
| 注册表阶段读取还没加载的配置导致崩溃 | 配置读取包一层安全回退，取不到就用默认值 |
| `MixinModifyRecalculationTarget$Energy` 依赖脆弱的字节码匹配 | 改为 `RETURN` 注入 + 虚方法重放，可选模组的目标不再要求匹配成功 |
| `enchantableMekaGear` 对 Meka 装甲无效 | 改为运行时注入 `ItemMekaSuitArmor`，不再依赖 Mekanism 的 lambda 编号 |
| 1.20.1 读档时强化升级等级从 16 掉回 8 | `Upgrade.getMax()` 的改写会被 10.4 的 `Upgrade.buildMap` 绕过（那里直接读 `maxStack` 字段），给这次字段读取补上 `require = 1` 的注入 |
| 先插原版升级、再插强化升级不生效，要挖掉重插一个原版升级才恢复 | 原版 `recalculateUpgrades` 只认 SPEED/ENERGY，插强化升级时它没有任何动作、缓存不刷新；现在插拔任意升级后强制重算 |
| 「放置方块恢复配置 NBT」在生产环境失效 | core 源码集的 refmap 没被打进 jar，补上后参考映射可用 |
| 创造模式页签标题显示英文 | 不再硬编码 `Component.literal`，改走语言文件 |
| 可选模组的 `@Pseudo` 注入目标对不上就启动崩溃 | 这些注入改为 `require = 0`；两份 mixin 配置的 `defaultRequire` 仍保持 1 |
| 配置保存是异步的，侧配置按钮要退出界面才刷新状态 | 保存改为同步落盘；按钮每帧按当前配置与朝向刷新可用状态 |
| 模组自己的 NBT 分区在 1.20.1 上从未生效，强化升级一直混在原版 `upgrades` 列表里 | 存档注入按名字抓局部变量（10.4 里叫 `nbtTags`，不是合成名 `arg1`）而静默失效；改为不写死名字。序号区间也差了一位，第一个追加升级（强化速度）永远分不进自己的标签，一并修正 |
| 分区修好后，老存档的强化升级会在读档时被当成「别人的升级」清掉 | 读档时若没有自己的标签就原样交回、不做清理；老存档首次读入后会自动迁移进新标签 |

## ⚠️ 已知问题

- **启动日志里的 `Error loading class: com/jerry/mekextras/...`**：这是给 1.21.1 预留的目标探测，1.20.1 上对应扩展包的包名是 `com.jerry.mekanism_extras`，探测不会命中任何东西，无害。
- **`configuration.*` 那批语言键暂无消费者**：它们是为和 1.21.1 分支共用语言文件保留的，1.20.1 上不会显示。
- **Meka 装备附魔要手动加标签**：`enchantableMekaGear` 打开后物品才能附魔，但还需要你手动把物品加进可附魔标签。
- **四台机器的吞吐只有公式推导**：电动泵、数字型采矿机、流体装填器、公式装配器在 `maxOperationsPerTick` 约束下的实际吞吐，以及上表的倍率，没有在服务器上实测过，都是按公式算出来的。
- **被旧 jar 砍掉数量的存档要重插一次升级**：用过修复之前的版本的话，读档时强化升级数量会被砍到 8 并写回磁盘，装上这一版后需要手动重新插一次升级。

## 🛠️ 构建

环境要求：**JDK 17**、可访问 Maven 仓库的网络。

```
gradlew build
```

## 📜 更新日志

### 20.1-1.0.0

1.20.1 上的第一个版本：把 Empowered 与 Unleashed 合成一个模组，并带上在 1.21.1 分支上验证过的修复。

- 合并为单一 jar、单一 mod id（`mekanism_empowered_unleashed`），Empowered 原来独立的 Core 前置并入本体，全部代码统一为 Kotlin
- 升级中枢改为定点注入，`MekanismUtils` 不再被 `@Overwrite` 整段接管
- 强化升级数量不再在读档时从 16 掉回 8：`Upgrade.buildMap` 那次字段读取现在也被改写覆盖
- 插入顺序 bug 修复：插了强化升级后缓存立即重算，不用再挖掉重插原版升级
- 「放置方块恢复配置 NBT」在生产环境恢复生效：core 源码集的 refmap 打进 jar
- 创造模式页签标题改为可翻译文本，不再硬编码英文
- 可选模组的 `@Pseudo` 目标注入改为 `require = 0`，目标字节码对不上时不再启动崩溃
- 配置保存改为同步落盘；侧配置按钮状态按当前配置实时刷新
- 强化升级的效果提示改用真实公式，泵与输入输出容量两处都对上了实现
- 一并带上 1.21.1 上的修复：能量升级超过 8 个不减耗、强化能量容量多算一个平方、四台机器补次上限硬编码 128、两层上限绑在一个旋钮上、能量抵消比例写死、`enchantableMekaGear` 对 Meka 装甲失效

## ⚖️ 许可证与署名

- 本合并版基于 [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered)（Copyright © 2025 Lapis，MIT）与 Mekanism Unleashed（Copyright © 2024 WhitePhant0m，MIT）
- 合并与修改：Copyright © 2026 zaixiayesheng，MIT License
- 完整协议见 [LICENSE](./LICENSE)

## 🐛 反馈

发现 bug 或想提建议，请到 [GitHub Issues](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)。
