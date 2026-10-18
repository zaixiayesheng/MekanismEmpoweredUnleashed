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

[这是什么](#-这是什么) · [特性](#-特性) · [安装](#-安装) · [兼容性](#-兼容性) · [配置](#-配置) · [升级体系与数值](#-升级体系与数值) · [设计取舍](#-设计取舍) · [已知问题](#-已知问题) · [构建](#-构建) · [更新日志](#-更新日志) · [许可证与署名](#-许可证与署名) · [反馈](#-反馈)

</div>

---

## 📖 这是什么

本项目把《通用机械：增强》和《通用机械：解放》的玩法合并成一个模组：两者都改 `MekanismUtils` 里的升级计算，同一段方法体只能保留一套，所以同装时只会有一方的效果生效；本模组把两层逻辑重排后放进同一个 jar。

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

## 🔧 设计取舍

| 关注点 | 本模组的做法 |
|---|---|
| 注入方式 | 统一用 `@ModifyArg` / `@ModifyReturnValue` 定点注入，两层注入点错开、互不覆盖 |
| 能量升级的抵消 | 在 `maxUpgrades` 允许的范围内按比例生效 |
| 强化能量的容量 | 指数与速度、每 tick 能耗同阶（默认满配 ×2,000） |
| 补次上限 | 泵、数字型采矿机、流体装填器、公式装配器的补次上限由 `maxOperationsPerTick` 控制，默认跟随 `maxUpgrades` |
| 升级提示 | 显示 `强化倍率^(已装/上限)` 的实际倍率 |
| 强化能量公式 | 按配置上限归一，抵消比例由 `empoweredEnergyOffsetRate` 决定 |
| 强化倍率稀释 | 按 `倍率^(16/N)` 随 `maxUpgrades` 自动稀释 |
| 两层上限 | 拆成 `maxUpgrades`（原版线 + 解锁门槛）与 `maxEmpoweredUpgrades`（强化线） |
| 难度调节 | `vanillaEnergyOffsetRate`（1.0）与 `empoweredEnergyOffsetRate`（0.5） |
| 配置读取 | 全部读取点带默认值回退，配置未加载时也不会出错 |
| 注入点稳定性 | `MixinModifyRecalculationTarget$Energy` 改为 RETURN 注入 + 虚方法重放，不依赖字节码形状 |
| `enchantableMekaGear` | 运行时注入 `ItemMekaSuitArmor`，不依赖 lambda 编号 |
| 打包 | 单一模组、单一 jar |
## 🛠️ 构建

环境要求：**JDK 17**、可访问 Maven 仓库的网络。

```
gradlew build
```

## 📜 更新日志

### 20.1-1.0.4

- 恢复大型机器（通用机械：更多机器）的原版速度语义：baseline 仍然是 `2^原版速度`，强化速度只在其上叠加 `2^(强化数 + 1)`，与 20.1-1.0.1 及更早版本一致。此前那版把它换成小机器同口径后，16 个原版速度升级只剩 448 操作/tick（原版是 4,194,304），大型回旋退化到小型回旋的速度、原版速度升级像被砍掉了。
- 新增固定能量补偿，按插了多少个能量升级降低单次耗能：`除数 = 2^(原版能量数/2 + 强化能量数/2)`（上限 2^40），不提供配置项。32 原版 + 32 强化能量、两个抵消率都设 0 时，单次耗能从 1.0E11 FE 压到约 23 FE，操作数顶到整数安全线 1,073,741,696 次/tick，同时每 tick 实打实吃掉整条储能——补偿是为了让机器跑得起来，不是让电免费；想省电只能调强化能量抵消，默认 0.5。补偿只作用于 `com.jerry.meklm` 的大型机器，不插能量升级时完全不生效。
- 整数保护：`baseOperations × baseline` 按 int 余量钳制，32 个原版速度 + 32 个强化速度、两个能耗抵消率都设成 0 也不会越过 int 上限；小机器与工厂走同一套钳制路径。
- 保留能耗钳制（每 tick 能耗不超过储能上限），只在储能真的不够时降速运行；速度上限的整数保护不变，所以最高速配置下的能耗依旧是实打实的极高开销。

### 20.1-1.0.3

- 修正大型机器（通用机械：更多机器）的速度换算：每 tick 操作数改为和小机器同一个口径（`倍率^(总速度/8)`），不再沿用原版 `2^速度 × baseOperations`。16 个速度升级时操作数从 65,536 降到 128 左右，机器不会再领到远超能量条能支付的次数，能量栏里那串「和储能上限同一量级」的数字（例如 2.14G FE/t）随之消失。
- 修正 20.1-1.0.2 的回归：上一版只在装了强化速度时才改写 baseline，插入强化速度反而把操作数从几十万压到几千、机器越升越慢。现在换算始终生效，操作数随速度单调上升。
- 换算结果向上取整、并对整数上限留出余量，插满 32 个速度也不会溢出停机。
- 已在开发服务器实测：16 原版速度 + 16 原版能量时每 tick 操作数为 128；再加 16 强化速度升到 10,048。

### 20.1-1.0.2

- 大型机器（通用机械：更多机器）单独适配：原先把原版速度与强化速度折算进 2 的幂次再相乘，升级堆到十几级后每 tick 操作数溢出成负数，机器直接判「能量不足」并停摆；现在按小机器的口径正面算出目标操作数，16/16、32/32 甚至强化插满都只受 int 上界保护，不再假停
- 每 tick 能耗不再超过储能上限：Mekanism 用「储能 ÷ 每 tick 能耗」算这一 tick 能跑几次操作，算出 0 就会同时挂上「能量不足」与「能量不足以最高速度运行」并强制停机；钳制之后最坏情况只是降速运行
- 固定耗能容器（大型电解分离机用的那种）也走同一道钳制：它自己重写了每 tick 能耗的计算，原先完全绕开 `MekanismUtils`，我们的缩放与上限对它都不生效

### 20.1-1.0.1

- 升级现在真正存进模组自己的 NBT 分区：原先读档注入按名字抓局部变量（10.4 里叫 `nbtTags`），一直静默失效，追加升级全混在原版列表里
- 修掉序号区间差一位：强化速度（序号 7）此前被排除在分区范围外，永远分不进自己的标签
- 老存档迁移：存档里没有自己的标签时不再清理，强化升级会在第一次读档后自动搬进新标签，不会被当成「别的模组的升级」删掉
- 可选模组目标改用 1.20.1 的真实包名 `com.jerry.mekanism_extras`（原来是 1.21.1 的 `com.jerry.mekextras`，永远匹配不到），并补上 mekaf / mekmm 两个整合基类

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
