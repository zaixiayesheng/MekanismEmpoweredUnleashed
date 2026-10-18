<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">通用机械：增强·解放</h1>

<h3 align="center">Mekanism: Empowered Unleashed</h3>

<div align="center">

通用机械（Mekanism）增强模组，由 **Mekanism: Empowered** 与 **Mekanism Unleashed** 合并而成。

English version: [README_EN.md](./README_EN.md)

[![Release](https://img.shields.io/github/v/release/zaixiayesheng/MekanismEmpoweredUnleashed?style=flat-square&label=Release&color=blue)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/releases)
[![CurseForge Downloads](http://cf.way2muchnoise.eu/1702076.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![CurseForge Versions](http://cf.way2muchnoise.eu/versions/1702076_latest.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![Minecraft 1.21.1](https://img.shields.io/badge/Minecraft-1.21.1-green.svg?style=flat-square)](https://www.minecraft.net)
[![NeoForge 21.1](https://img.shields.io/badge/NeoForge-21.1-orange.svg?style=flat-square)](https://neoforged.net)
![Kotlin 2.4.0](https://img.shields.io/badge/Kotlin-2.4.0-7F52FF.svg?style=flat-square&logo=kotlin&logoColor=white)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](./LICENSE)
[![Issues](https://img.shields.io/badge/Issues-GitHub-black.svg?style=flat-square&logo=github)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)

</div>

<div align="center">

[这是什么](#-这是什么) · [特性](#-特性) · [安装](#-安装) · [兼容性](#-兼容性) · [配置](#-配置) · [升级体系与数值](#-升级体系与数值) · [设计取舍](#-设计取舍) · [构建](#-构建) · [更新日志](#-更新日志) · [许可证与署名](#-许可证与署名) · [反馈](#-反馈)

</div>

---

## 📖 这是什么

本项目把《通用机械：增强》和《通用机械：解放》的玩法合并成一个模组：两者都改 `MekanismUtils` 里的升级计算，同一段方法体只能保留一套，所以同装时只会有一方的效果生效；本模组把两层逻辑重排后放进同一个 jar。

这个项目把两者合成一个模组：

- 单一 jar、单一 mod id（`mekanism_empowered_unleashed`），不再需要独立的 Core 前置；
- 全部代码统一为 Kotlin；
- 注入点重新排开，两层效果按顺序相乘，不再互相覆盖。

只兼容原版 Mekanism（1.21.1 / 10.7.19.x）。

## ✨ 特性

- **16+16 双层升级体系**：原版速度/能量升级（第一层，默认上限 16）+ 强化速度/能量升级（第二层，第一层插满后解锁）
- **两层上限分开配**：`maxUpgrades`（8–32，默认 16）是原版线上限，也是强化线的解锁门槛；`maxEmpoweredUpgrades`（1–32，默认 16）单管强化线，只影响强化层的加成分成几级
- **强化倍率自动稀释**：`maxUpgrades` 调高时 `maxUpgradeMultiplier` 按 `倍率^(16/maxUpgrades)` 自动变小（上限 32 时 20 → 4.47），不会因为调高原版上限就速度和储能一起失控
- **修掉减耗封顶**：Unleashed 原来的能量公式把能量升级的效果封顶在 8，插 32 个和插 8 个耗电一样；现在跟着配置上限走
- **四台机器支持每 tick 多操作**：泵、数字型采矿机、流体装填器、公式装配器（速度超过 1 tick/操作后继续加速）
- **Meka 装备附魔开关**（`enchantableMekaGear`，继承自 Unleashed，默认关闭）
- **上游的模组联动**：Mekanism Extras、More Machine、Evolved Mekanism、Mekanism Generators 等

## 📦 安装

| 文件 | 必需 | 说明 |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.21.1-21.1-*.jar` | ✅ | 本模组（唯一文件） |
| `kotlinforforge-5.12.0-all.jar` | ✅ | Kotlin 运行时前置 |
| Mekanism 1.21.1（官方 10.7.19.x） | ✅ | 本体依赖 |

- 平台：NeoForge 21.1.x，Minecraft 1.21.1
- **不要与原版同时安装**：合并版已包含 `mekanism-unleashed` 和 `mekanism-empowered` 的全部功能，原版两者都不要装
- `-sources.jar`、`-api.jar` 为开发者配套，无需安装

## 🤝 兼容性

### 与无用之物（UselessMod）共存

两者可以一起安装，强化升级、Fast Item Eject 等功能都正常工作，无用之物自身的合金炉、牛肉工具、矿物生成器等内容也不受影响。

1.0.3 及以前本模组在元数据里声明过 `useless_mod` 不兼容，那时的表现是两个模组装在一起游戏直接起不来；这个声明在 1.0.4 已经去掉。

具体是哪几处撞在一起、各自怎么处理的，写在源码注释里（`UselessModCompat`、`mixin/unleashed` 下的混入类）。

## ⚙️ 配置

配置文件：`config/MekanismEmpowered Unleashed/general.toml`

| 配置项 | 默认值 | 范围 | 作用 |
|---|---|---|---|
| `maxUpgrades` | 16 | 8–32 | 原版速度/能量升级的上限，也是强化升级的解锁门槛（插满这么多个才解锁） |
| `maxEmpoweredUpgrades` | 16 | 1–32 | 强化速度/能量升级的上限。只决定强化层的总加成拆成几级，插满时的总加成由 `maxUpgradeMultiplier` 决定 |
| `maxUpgradeMultiplier` | 20 | ≥1 | 强化线插满时的效果倍率。`maxUpgrades` 高于 16 时自动稀释为 `倍率^(16/maxUpgrades)`（上限 24 → 7.37、上限 32 → 4.47） |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0–1.0 | 原版能量升级抵掉多少额外耗能。1.0 是原版行为，速度数和能量数相同时每操作耗电不变；调低更费电 |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0–1.0 | 强化能量升级的抵消率。默认只抵一半，所以强化层不是能量中性的；填 1.0 恢复每操作总能耗 ×1 |
| `enchantableMekaGear` | false | — | 原版 Meka 装备不能附魔；开启后允许，但要自己把物品加进可附魔标签 |
| `maxOperationsPerTick` | 128 | 1–10000 | 泵、数字型采矿机、流体装填器、公式装配器每 tick 的补次上限。速度超过这个量级，对这四台机器不再有可见效果；调高会加重服务器负担 |
| `autoScaleOperationsPerTick` | true | — | 上一项是否跟着 `maxUpgrades` 抬高（只升不降）。默认开启，只调 `maxUpgrades` 就够；关掉后它才是硬上限 |

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

| `maxUpgrades` | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

`maxEmpoweredUpgrades` 不改变上表的合计，只改变强化层被拆成几级（16 级时每级约 +20%、32 级时每级约 +10%）。

> 以前 32 档合计 ×200,000，储能也涨到 20 万倍，叠在 Mekanism Extras 这类高等级机器上容易顶到 `Long.MAX`；现在 32 档是 ×44,700。

### 耗电怎么算

速度升级让机器每 tick 耗电变多，能量升级让它变少，两者相抵的比例就是抵消率。原版线默认 100% 抵消（`vanillaEnergyOffsetRate = 1.0`），速度数和能量数相同时，每加工一件物品的总耗电回到原版；强化线默认只抵一半（`empoweredEnergyOffsetRate = 0.5`），插满后每操作耗电高于原版。只插速度不插能量，耗电会暴涨，这是原版就有的设定。

两个抵消率控制两层能量升级各抵掉多少额外耗能，1.0 完全抵消、0.0 完全不抵消，整合包和服主可以直接拿来调难度：

| `empoweredEnergyOffsetRate` | 每 tick 能耗 | 每操作总能耗 | 满电续航 |
|---|---|---|---|
| 1.0（旧行为） | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

`vanillaEnergyOffsetRate` 同理作用于原版线（默认 1.0）；把它调低，所有机器在提速时都会更费电。

### 容量为什么是 ×2,000

容量的强化系数和速度、能耗同阶，容量倍率 = 原版线 ×100 × 强化层 ×20 = ×2,000。默认抵消率下每 tick 能耗是 ×8,944，所以满电续航降到 ×0.22（抵消率填 1.0 时是 ×1）。升级放大的是功率，想更耐跑得另加容量升级。

### 四台机器另有一条上限

电动泵、数字型采矿机、流体装填器、公式装配器各有一个每 tick 补次循环，受 `maxOperationsPerTick`（默认 128）约束。速度升到万级倍率后，理论需求远超这条线，这就是"插满速度升级却看不出更快"的原因。`autoScaleOperationsPerTick = true` 时这条线跟着 `maxUpgrades` 抬高（原版倍率^(N/8)：16 → 128、24 → 1000、32 → 10000），只调 `maxUpgrades` 就能让四台机器一起变快；关掉开关，它就是硬上限，也可以直接调 `maxOperationsPerTick`。

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

环境要求：**JDK 21**、可访问 Maven 仓库的网络。

```
gradlew build
```

## 📜 更新日志

### 21.1-1.0.10

- 恢复大型机器（通用机械：更多机器）的原版速度语义：baseline 仍然是 `2^原版速度`，强化速度只在其上叠加 `2^(强化数 + 1)`，与 21.1-1.0.7 及更早版本一致。此前那版把它换成小机器同口径后，16 个原版速度升级只剩 448 操作/tick（原版是 4,194,304），大型回旋退化到小型回旋的速度、原版速度升级像被砍掉了。
- 新增大型机器能量上限补偿：把「每 tick 总耗能」压在整数上限 2,147,483,647 FE 以内。操作数上限 = `4 × 原版速度数 × min(原生 2^速度 + 强化叠层, 整数安全余量)`，单次耗能上限 = `2,147,483,647 / 操作数上限`。插满 32 速度 + 32 强化速度时操作数 = 1,073,741,696 次/tick、单次耗能 2 FE，整机正好以 2,147,483,646 FE/t 顶着上限跑满；32 原版速度、两个抵消率都设 0 时同样被压到 21 亿，不会再出现 32 亿、300 亿这种越界数字。补偿只作用于 `com.jerry.meklm` 的大型机器，不提供配置项，也不会让电变免费。
- 整数保护：`baseOperations × baseline` 按 int 余量钳制，32 个原版速度 + 32 个强化速度、两个能耗抵消率都设成 0 也不会越过 int 上限；小机器与工厂走同一套钳制路径。
- 保留能耗钳制（每 tick 能耗不超过储能上限），只在储能真的不够时降速运行；速度上限的整数保护不变，所以最高速配置下的能耗依旧是实打实的极高开销。

### 21.1-1.0.9

- 修正大型机器（通用机械：更多机器）的速度换算：每 tick 操作数改为和小机器同一个口径（`倍率^(总速度/8)`），不再沿用原版 `2^速度 × baseOperations`。16 个速度升级时操作数从 65,536 降到 128 左右，机器不会再领到远超能量条能支付的次数，能量栏里那串「和储能上限同一量级」的数字（例如 2.14G FE/t）随之消失。
- 修正 21.1-1.0.8 的回归：上一版只在装了强化速度时才改写 baseline，插入强化速度反而把操作数从几十万压到几千、机器越升越慢。现在换算始终生效，操作数随速度单调上升。
- 换算结果向上取整、并对整数上限留出余量，插满 32 个速度也不会溢出停机。
- 已在开发服务器实测：16 原版速度 + 16 原版能量 + 16 强化能量时每 tick 操作数为 128；再加 16 强化速度升到 10,048。

### 21.1-1.0.8

- 单独适配「通用机械：更多机器」的大型机器（大型化学灌注机、大型颜料混合器、大型回旋式气液转换机、大型电解分离机、大型太阳能中子活化器）：它们把每 tick 操作数算成 `baseOperations × 2^原版速度` 的整数乘积，速度一高就溢出成负数，Mekanism 拿这个负数去算「储能 ÷ 每 tick 能耗」得到 0，于是同时挂上「能量不足」和「降速」两个状态并强制停机——界面上就是插满升级后机器不动了。现在 baseline 改成和小机器同一个口径（`倍率^(总速度/8)`），并对整数上限留出余量。
- 追加一道保险：每 tick 能耗不允许超过储能上限。最坏情况只是降速运行，不会再因为「储能 ÷ 每 tick 能耗 = 0」停机。大型电解分离机用的 `FixedUsageEnergyContainer` 绕开了原版能耗计算，也一并纳入。
- 已在开发服务器实测 16/16 与 32/32（原版速度、强化速度各 32）两组升级：读档重算不再溢出，算出的每 tick 操作数为正且不越界。

### 21.1-1.0.7

- 修复强化速度（`empowered_speed`）写不进模组自己的 NBT 分区：分区序号区间差一位，第一个追加的升级被关在范围外，一直混在原版 `upgrades` 列表里。现在区间从追加的第一个升级本身开始。
- 老存档自动迁移：读档时若分区里还没有这一份，会先从原版列表里把它捡回来再清理列表，数量不丢，之后就写在自己的分区里。
- 存档里完全没有本模组的分区时不再清理原版列表，避免把数量直接删掉。
- 已在 NeoForge 1.21.1 专用服务器上实测：新式分区原样往返；老存档里的 `{type:7,amount:9}` 读档后变成分区里的 `empowered_speed: 9`；没有分区、以及只有旧键 `additional_upgrades` 的存档都正常；`save-all flush` 后整机重启，数量不变。

### 21.1-1.0.6

- 修复「自动输入升级」侧面开关的网络包不做校验：任何客户端都能翻转别人机器上的开关并写进存档。现在和 Mekanism 自己的机器包一样先过 `IBlockSecurityUtils.canAccess`。
- 修复数字型采矿机补采是白挖的：补的每一次现在都走完整的原版 tick，含能量检查与原版延迟，和泵、流体装填器、公式装配器同一套机制。
- 修复 `Temp.inject` 没有异常保护：补次过程中抛一次异常会让该线程一直停在「注入中」，之后泵、流体装填器、公式装配器、数字型采矿机的补次全部退化成每 tick 一次。
- 修复配置保存是异步的：在游戏里改完配置马上退出时改动可能丢，现在同步落盘，保留三次重试。
- 修复侧配置按钮的可点状态只在界面打开时算一次：开着界面在侧配置页改开关后按钮会变色但点不动，现在每帧刷新。
- 修复物品形态的能量容量显示不更新：拆掉或装上原版能量升级后，tooltip 里的储能还是旧值。
- 修复 IO 容量升级的效果说明：旧文案按每级 +3200% 算，满级会显示 +51200%，实际流体/能量/化学是 ×33、物品是 ×8，现在按已装比例显示两行。
- 配置说明里最大升级倍率不随上限增长的说法不准确，原版那条线本身仍在增长，已改。
- 创造模式页签的名字改成可翻译文本：中文环境显示「通用机械：增强·解放」，以前是硬编码的英文。
- 兼容性加固：25 个可选模组目标的 `@Pseudo` 注入不再因为目标字节码对不上而在启动时崩溃（`defaultRequire = 0`）；注入点补上 `ordinal`、完整方法描述符与 `require = 0`；删掉一个从不生效的死 mixin。

### 21.1-1.0.5

- 新增 `maxEmpoweredUpgrades`（默认 16，范围 1–32），把强化线上限和原版线拆开，它只决定强化层的总加成拆成几级；`maxUpgrades` 仍是原版线上限，兼强化线解锁门槛。
- 强化倍率改为随原版上限自动稀释（`maxUpgradeMultiplier^(16/maxUpgrades)`，只压不抬）。以前 `maxUpgrades` 调到 32 时合计 ×200,000、储能也涨 20 万倍，容易顶到 `Long.MAX`；现在 32 档是 ×44,700，默认 16 档不变，每级增量随上限变小（16 档约 +20.6%/级，32 档约 +9.7%）。
- 修掉强化能量升级容量倍率多算的一个平方：满配容量从 ×40,000 改成 ×2,000，与速度和每 tick 能耗同阶。
- 新增 `maxOperationsPerTick`（默认 128，范围 1–10000）和 `autoScaleOperationsPerTick`（默认 true）：泵、数字型采矿机、流体装填器、公式装配器的每 tick 补次上限不再写死 128，开着自动缩放时跟着 `maxUpgrades` 抬高（16 → 128、24 → 1000、32 → 10000）。
- 新增 `vanillaEnergyOffsetRate`（默认 1.0）和 `empoweredEnergyOffsetRate`（默认 0.5），分别控制两层能量升级抵掉多少额外耗能。默认下强化层不再是能量中性的：插满时每操作耗电 ×4.47、满电续航 ×0.22；填 1.0 回到旧行为，整合包可以拿它锁难度。
- 修复 `enchantableMekaGear` 对 Meka 装甲无效：旧实现注入的是 `MekanismArmorMaterials` 的编译期 lambda，材质在类初始化时就构造好了，那一刻配置还没加载，只能回退 false。改为运行时注入 `ItemMekaSuitArmor`，也不再依赖 Mekanism 的 lambda 编号。
- 修复强化速度/能量升级的 tooltip 显示的不是真实数值：强化速度显示 `2^已装`、强化能量按原版倍率 10 算、泵的文案写死 +100%/级；现在统一显示 `强化倍率^(已装/上限)`。

<details>
<summary>更早的版本</summary>

### 21.1-1.0.4

- 移除了与无用之物（UselessMod）的不兼容声明，两个模组可以一起安装
- 修复了两者同时安装时强化升级失效的问题，Fast Item Eject 也恢复正常

### 21.1-1.0.3

- 移除了内嵌的旧 id 兼容子模组（桥接），主 jar 恢复单一 mod id
- MekaJade 的升级图标支持改由 MekaJade Upgrades Fixed 直接适配，不再需要内置桥接

### 21.1-1.0.2

- 修复了资源蜜蜂：创世（Productive Bees Genesis）启动时把本模组误判为不支持的依赖并报错的问题

### 21.1-1.0.1

- 修复强化升级必须按顺序插入才生效的问题，现在无论先插哪个都立即生效。
- 修复泵、流体装填器、公式装配器、数字型采矿机插满速度升级后仍然每 tick 只干一次的问题，现在能一 tick 干多次，最多 128 次。
- 修复 MekaJadeUpgrades 显示增强升级显示异常。

</details>

## ⚖️ 许可证与署名

- 本合并版基于 [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered)（Copyright © 2025 Lapis，MIT）与 Mekanism Unleashed（Copyright © 2024 WhitePhant0m，MIT）
- 合并与修改：Copyright © 2026 zaixiayesheng，MIT License
- 完整协议见 [LICENSE](./LICENSE)

## 🐛 反馈

发现 bug 或想提建议，请到 [GitHub Issues](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues)。

提交时请附上游戏版本、Mekanism 版本与 `logs/latest.log`。
