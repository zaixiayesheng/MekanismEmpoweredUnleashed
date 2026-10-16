# 工程说明与变更记录

本仓库是 **Mekanism: Empowered** 与 **Mekanism Unleashed** 的合并版。

单一模组（mod id `mekanism_empowered_unleashed`），面向 Minecraft 1.21.1 / NeoForge 21.1，只适配原版 Mekanism（10.7.19.x）。

署名与许可证（MIT）：

- Mekanism: Empowered — © 2025 Lapis
- Mekanism Unleashed — © 2024 WhitePhant0m
- 合并与修改 — © 2026 zaixiayesheng

## 为什么合并

两个 mod 单独装会互相冲突。

它们都改 Mekanism 的全局数值中枢 `MekanismUtils`。Unleashed 用 `@Overwrite` 整段替换方法体，Empowered 用定点注入。Overwrite 生效后原版指令消失，Empowered 要么启动即崩（`InvalidInjectionException`），要么被整段抹掉。

另外 Unleashed 的能量公式把能量升级计效封顶在 8，插 32 个和插 8 个消耗一样。

## 主要改动

1. **冲突消解**：`MixinMekanismUtils.kt` 的 6 个 `@Overwrite` 全部改为
   `@ModifyArg` / `@ModifyReturnValue`，注入点与 Empowered 刻意错开
   （Math.pow 指数 vs ceilToLong 参数 / RETURN 分层），两层效果按顺序相乘。
2. **全 Kotlin 统一**：Unleashed 移植部分（10 个 Mixin + 工具类）全部用 Kotlin 重写。
   注意 Mixin 类里的 companion object 必须 `private`（Mixin 禁止非私有静态字段）。
3. **单模组**：原独立的 Core 已并入本体，一个 jar、一个 mod id、一份 mods.toml
   （注册两份 mixin 配置）；EasyNestConfig 以 JarJar 内嵌。
4. **配置**：`maxUpgrades`（8–32，默认 16）控制原版速度/能量升级上限，并作为强化
   升级的解锁门槛；`maxEmpoweredUpgrades`（1–32，默认 16）单独控制强化线上限
   （只决定总加成被拆成几级）；`maxUpgradeMultiplier`（默认 20）是强化线插满时的
   总加成，`maxUpgrades` 高于 16 时自动稀释为 `倍率^(16/maxUpgrades)`；另有
   `enchantableMekaGear` 与 `maxOperationsPerTick`（四台自补次机器的每 tick 上限，
   默认 128）及 `autoScaleOperationsPerTick`（默认 true：该项自动跟随 `maxUpgrades`
   抬高，只升不降）；以及两个「抵消率」难度旋钮 `vanillaEnergyOffsetRate`（默认 1.0）
   与 `empoweredEnergyOffsetRate`（默认 0.5），分别决定两层能量升级能抵掉多少比例
   的额外耗能，整合包/服主可据此锁定难度。
5. **修复**：
   - 能量减耗封顶跟随配置（原硬编码 8）
   - tier-2 能耗公式按配置上限归一；抵消比例改由配置决定（见下条）
   - 注册表阶段读取未加载配置导致的崩溃（所有读取点带默认值回退）
   - `MixinModifyRecalculationTarget$Energy` 由字节码表达式匹配改为
     RETURN 注入 + 虚方法重放，不再受 Mekanism 版本字节码差异影响
   - tier-2 容量指数多乘了一个 2（默认满配 ×400 而非 ×20），改为与速度/能耗
     同阶（容量 ×40,000 → ×2,000）
   - 泵/数字型采矿机/流体装填器/公式装配器的补次上限由硬编码 128 改为配置项
     `maxOperationsPerTick`：maxUpgrades=32 时理论需求约 20 万次/tick，被 128
     截断，强化速度由此失效
   - 能量抵消率原本写死在公式里：新增 `vanillaEnergyOffsetRate`（默认 1.0）与
     `empoweredEnergyOffsetRate`（默认 0.5）。默认配置下强化层不再能量中性，
     每操作总能耗 ×1 → ×4.47、满电续航 ×1 → ×0.22
   - `enchantableMekaGear` 对 Meka 装甲永久失效：旧实现注入
     `MekanismArmorMaterials#lambda$static$7`，而材质在类初始化时构造，
     那一刻 SERVER 配置未加载只能回退 false；改为运行时注入
     `ItemMekaSuitArmor`（同 `MixinItemMekaTool` 的做法），顺带去掉了对
     编译期 lambda 编号的依赖
   - 强化升级的 tooltip 与实现不符：强化速度显示 `2^已装`、强化能量按原版倍率
     10 计算、泵的文案写死 "+100%/级"；统一改走 `UnleashedUtils.empoweredMultiplier`
   - 补次上限新增自动缩放开关 `autoScaleOperationsPerTick`（默认 true），
     只调 `maxUpgrades` 就能让四台机器跟着变快
   - 强化线倍率不再固定：旧行为下 `maxUpgrades` 调到 32 时合计从 ×2,000 涨到
     ×200,000（储能同步，容易顶到 Long.MAX）；现在强化线自动稀释（32 档 → ×4.47），
     合计 ×44,700，默认 16 档不变
   - 两层上限分离为 `maxUpgrades` / `maxEmpoweredUpgrades` 两个配置项
6. **版本栈**：模组 `21.1-1.0.5`；KotlinForForge 5.12.0；Kotlin 2.4.0。

## 数值（默认 maxUpgrades=16，原版倍率 10，强化倍率 20）

16 速度 + 16 能量 + 16 强化速度 + 16 强化能量全满：

- 每操作耗时 ×1/2000，每 tick 能耗 ×8,944，每操作总能耗 ×4.47
- 能量存储 ×2,000，满电续航 ×0.22，吞吐 ×2000

调高 `maxUpgrades` 时（强化倍率自动稀释，有效值 = `20^(16/N)`）：

| maxUpgrades | 原版线满配 | 强化线有效倍率 | 合计 |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16（默认） | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

抵消率（1.0 完全抵消、0.0 完全不抵消）：

| empoweredEnergyOffsetRate | 每 tick 能耗 | 每操作总能耗 | 满电续航 |
|---|---|---|---|
| 1.0（旧行为） | ×2,000 | ×1 | ×1 |
| 0.5（默认） | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

`vanillaEnergyOffsetRate` 同理作用于原版线（默认 1.0）。

## 构建

JDK 21，`gradlew build`。

## 已知注意点

- 四台自补次机器（泵、数字型采矿机、流体装填器、公式装配器）的实际速度受
  `maxOperationsPerTick` 约束。默认 `autoScaleOperationsPerTick = true`，
  该上限自动跟随 `maxUpgrades`（= 原版倍率^(N/8)：N=16 → 128、
  N=24 → 1000、N=32 → 10000），所以只调 `maxUpgrades` 即可；把它关掉时
  才需要玩家自行调高 `maxOperationsPerTick`（N=32 时第一层需求约 10^4 次/tick，
  连同第二层约 2×10^5 次/tick，仍远超上限，第二层在四台机器上只剩很小增量）。
- 第二层（强化）的速度/能耗/容量倍率按 `有效倍率^(已装/强化上限)` 计算，有效倍率
  = `maxUpgradeMultiplier^(16/maxUpgrades)`（上限 32 时 20 → 4.47）。所以强化层
  与原版线上限挂钩：上限越大，强化层每一级的相对增量越小（16 档每级约 +20.6%、
  32 档约 +9.7%），但合计倍率始终随上限单调上升（×2,000 → ×44,700），
  不会像旧版那样冲上 ×200,000。`maxEmpoweredUpgrades` 只改变分级数，不改变合计。
- 存档中新增升级使用独立 NBT 命名空间 `mekanism_empowered_unleashed:*`，
  与 mod id 同步改名后，1.0.0 之前的测试存档中的强化升级会丢失（原版升级不受影响）。
- `en_us.json` 由 datagen 生成，重跑 datagen 会重新生成（配置键自动带出，无需手工维护）。
- 强化层默认不再能量中性：`empoweredEnergyOffsetRate = 0.5` 时插满强化线，每操作耗电是原版的 ×4.47、满电续航 ×0.22（旧行为都是 ×1）。要恢复旧观感就把该项填 1.0；想更硬核就往下调（0.0 时每操作耗电 ×20）。
