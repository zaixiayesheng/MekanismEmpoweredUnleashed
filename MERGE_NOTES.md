# 工程说明与变更记录

本仓库是 **Mekanism: Empowered** 与 **Mekanism Unleashed** 的合并版。

单一模组：mod id `mekanism_empowered_unleashed`，一个 jar、一份 `mods.toml`，Empowered 原来独立的 Core 已并入本体；全部代码统一为 Kotlin。

本分支面向 Minecraft 1.20.1 与 Forge 47.4.0（构建用 JDK 17），只适配原版 Mekanism 的 10.4 线（10.4.16+）。

署名与许可证（MIT）：

- Mekanism: Empowered — © 2025 Lapis
- Mekanism Unleashed — © 2024 WhitePhant0m
- 合并与修改 — © 2026 zaixiayesheng

## 为什么合并

两个上游模组都要改 `MekanismUtils` 里那套升级效果的算法。Unleashed 的做法是用 `@Overwrite` 把整个方法体换掉，Empowered 的做法是在原版方法里定点注入；只要 Unleashed 的覆写先生效，原版的指令就不存在了，Empowered 的注入要么在启动阶段崩溃，要么不报错但静默失效。一个方法只能有一个 `@Overwrite`，所以这两者不能同时安装。

Unleashed 本身还有一处独立的问题：能量升级的效果被硬编码封顶在 8，插到第 9 个之后不再减耗，配置里把上限调高也没用；这条线同时把两层升级上限绑在同一个旋钮上，调到 32 时总倍率会冲到 ×200,000，储能也跟着涨 20 万倍，很容易顶到 `Long.MAX`。

合并的目标是让两者的功能都完整保留：升级中枢改为定点注入、注入点错开，两层效果按顺序相乘；同时把上面这些问题一起修掉。

## 主要改动

1. **冲突消解**：`MekanismUtils` 不再由 `@Overwrite` 整段接管，改为 `@ModifyExpressionValue` / `@Inject` 定点注入，两层升级的效果按顺序叠加。Unleashed 里唯一保留 `@Overwrite` 的是 `UpgradeUtils` 的两个纯显示方法（升级叠加提示），这两个方法设了更高的 mixin 优先级（2000），用来压过同样改它们的无用之物（UselessMod）。

2. **全 Kotlin 统一**：上游的 Java 代码全部改写为 Kotlin，需要的 `@Invoker` / `@Accessor` 等接口集中在 `mixin_impl` 下。Kotlin 的 companion object 必须是 private，否则 Mixin 会因为非私有静态字段报错。

3. **单模组**：原来独立的 Core 已并入本体，一个 jar、一个 mod id、一份 `mods.toml`。两份 mixin 配置（`mekanism_empowered_unleashed.mixins.json` 与 `mekanism_empowered_unleashed_core.mixins.json`）仍分属两个源码集，core 那份的 refmap 需要显式打进 jar，否则生产环境里读不到参考映射、注入静默失效。

4. **配置**：`maxUpgrades`（8–32，默认 16）控制原版速度/能量升级上限，并作为强化升级的解锁门槛；`maxEmpoweredUpgrades`（1–32，默认 16）单独控制强化线上限（只决定总加成被拆成几级）；`maxUpgradeMultiplier`（默认 20）是强化线插满时的总加成，`maxUpgrades` 高于 16 时自动稀释为 `倍率^(16/maxUpgrades)`；另有 `enchantableMekaGear` 与 `maxOperationsPerTick`（四台自补次机器的每 tick 上限，默认 128）及 `autoScaleOperationsPerTick`（默认 true：该项自动跟随 `maxUpgrades` 抬高，只升不降）；能量侧还有 `vanillaEnergyOffsetRate`（默认 1.0）与 `empoweredEnergyOffsetRate`（默认 0.5），分别决定两层能量升级能抵掉多少比例的额外耗能。滴管的速率与容量在 `tiers.toml` 里单独配置。

5. **修复**：
    - 读档掉级：强化升级的上限原本只挂在 `Upgrade.getMax()` 上，而 10.4 的读档路径走 `Upgrade.buildMap`，那一行直接读 `maxStack` 字段、绕过了改写，于是重新进存档会把等级从 16 砍到 8。现在给那次字段读取加了 `require = 1` 的注入。
    - 插入顺序：原版 `recalculateUpgrades` 只认 SPEED/ENERGY，插入我们的强化升级时它没有任何动作、缓存不刷新，表现为「先插原版速度/能量、再插强化升级不生效，要挖掉重插一个原版升级才恢复」。现在插拔任意升级后强制重算。
    - 「放置方块恢复配置 NBT」失效：core 源码集的 refmap 没有被打进 jar，生产环境读不到参考映射，该注入静默不生效。构建脚本已补上 `include("..._core.refmap.json")`。
    - 创造模式页签标题：原来硬编码 `Component.literal(MOD_NAME)`，中文环境也显示英文，现在走语言文件。
    - 可选模组目标：`@Pseudo` 注入改为 `require = 0`，目标字节码对不上时不再启动崩溃。没有全局降低 `defaultRequire`，两份 mixin 配置仍保持 1。
    - 配置保存：`save()` 改为同步落盘（保留重试），调用方按「返回即已落盘」行事；侧配置按钮的可用状态改成每帧按当前配置与朝向刷新，原版 `AbstractWidget.mouseClicked` 直接读 `active` 字段，所以不能用覆写 `isActive()` 的办法。
    - 升级提示：强化升级的 tooltip 原来按「每级都有满级加成」显示，比实现夸大 8 倍；现在显示 `强化倍率^(已装/上限)`，泵与输入输出容量两处都换成真实公式。
    - 一并带上 1.21.1 分支验证过的修复：能量升级超过 8 个不减耗（封顶值跟随 `maxUpgrades`）、强化能量容量多算一个平方（满配 ×40,000 → ×2,000）、四台机器补次上限硬编码 128、两层上限绑在一个旋钮上、能量抵消比例写死在公式里、注册表阶段读取未加载配置导致崩溃、`enchantableMekaGear` 对 Meka 装甲失效、`MixinModifyRecalculationTarget$Energy` 依赖脆弱字节码匹配（改为 `RETURN` 注入 + 虚方法重放）。

6. **版本栈**：模组 `20.1-1.0.0`；Forge `47.4.0`；Minecraft `1.20.1`；Kotlin for Forge `4.12.0`（必需依赖）；Kotlin `2.2.21`；Mixin `0.8.5`；MixinExtras `0.5.0`；EasyNestConfig `3.0.0`；Parchment `2023.09.03`；开发与构建使用 JDK 17。运行期声明的可选依赖：Mekanism Extras ≥1.5.0、Evolved Mekanism ≥1.2.1、Mekanism Elements ≥2.3、More Machine ≥1.2.1、Evolved Extras ≥1.4.1；声明不兼容：`mekanism_unleashed`、`mekanismtweaks`、`mekanismupgradesreborn`。

## 数值（默认 maxUpgrades=16，原版倍率 10，强化倍率 20）

以 16 速度 + 16 能量 + 16 强化速度 + 16 强化能量全满为例：

- 每操作耗时 ×1/2000，每 tick 能耗 ×8,944，每操作总能耗 ×4.47
- 能量存储 ×2,000，满电续航 ×0.22，吞吐 ×2000
- 容量的强化系数和速度、每 tick 能耗同阶（×100 × ×20 = ×2,000）；满电续航的下降来自默认抵消率只抵一半

调高 `maxUpgrades` 时（强化倍率自动稀释，有效值 = `20^(16/N)`）：

| maxUpgrades | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

| empoweredEnergyOffsetRate | 每 tick 能耗 | 每操作总能耗 | 满电续航 |
|---|---|---|---|
| 1.0 | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

## 构建

JDK 17，`gradlew build`。产物是单个 `MekanismEmpoweredUnleashed-1.20.1-20.1-1.0.0.jar`，另有 `-api.jar` 与 sources jar 供开发使用。

## 已知注意点

- 四台机器（电动泵、数字型采矿机、流体装填器、公式装配器）的每 tick 补次循环受 `maxOperationsPerTick` 约束。默认 `autoScaleOperationsPerTick = true`，该上限自动跟随 `maxUpgrades`（= 原版倍率^(N/8)：N=16 → 128、N=24 → 1000、N=32 → 10000），所以只调 `maxUpgrades` 即可；把它关掉时它才是硬上限，N=32 时第一层需求约 10^4 次/tick，落差会很明显。
- 第二层分级：`maxEmpoweredUpgrades` 只改变强化层被拆成几级，不改变合计（16 档每级约 +20.6%，32 档约 +9.7%）。
- 模组自己的 NBT 分区 `mekanism_empowered_unleashed:additional_upgrades` 在 1.20.1 上原先是失效的：`saveMap` 那次注入按名字抓局部变量（10.4 里叫 `nbtTags`，不是合成名 `arg1`）而静默无效，且序号区间差了一位（`(vanillaLastOrdinal + 1)..` 把第一个追加升级漏在外面）。两处都已修正，并补了迁移分支：老存档没有自己的标签时原样交回、不做清理，首次读入后自动搬进新标签。实测（RCON + `data get block`）：强化速度 9 会写成 `{empowered_speed: 9}`、原版列表只剩 type 0/6；旧标签名 `additional_upgrades` 能读并能改写为新名；原版 16 级 SPEED/ENERGY 存档、重启后仍是 16。
- 启动日志会出现 `Error loading class: com/jerry/mekextras/...`，那是给 1.21.1 预留的目标探测（1.20.1 的扩展包名是 `com.jerry.mekanism_extras`），无害。
- `configuration.*` 系列语言键在 1.20.1 上没有消费者，为与 1.21.1 分支共用语言文件而保留；`en_us.json` 由 datagen 生成，改完语言文件要重新跑 datagen。
- 强化层默认不再能量中性：`empoweredEnergyOffsetRate = 0.5` 时插满强化线，每操作耗电是原版的 ×4.47、满电续航 ×0.22（旧行为都是 ×1）。要恢复旧观感就把该项填 1.0，想更硬核可以往下调。
- 四台补次机器的吞吐与上表的倍率都只有公式推导，没有实测数值；`enchantableMekaGear` 打开后还需要手动把物品加进可附魔标签。
