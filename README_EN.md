<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">Mekanism: Empowered Unleashed</h1>

<h3 align="center">通用机械：增强·解放</h3>

<div align="center">

An upgrade expansion for Mekanism: merges *Mekanism: Empowered* and *Mekanism Unleashed* into a single mod and adds a second, empowered tier on top of the vanilla upgrades.

Chinese version: [README.md](./README.md)

[![Release](https://img.shields.io/github/v/release/zaixiayesheng/MekanismEmpoweredUnleashed?filter=v20.1*&style=flat-square&label=Release&color=blue)](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/releases)
[![CurseForge](http://cf.way2muchnoise.eu/1702076.svg)](https://www.curseforge.com/minecraft/mc-mods/mekanism-empowered-unleashed)
[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-green.svg?style=flat-square)](https://www.minecraft.net)
[![Forge 47.4](https://img.shields.io/badge/Forge-47.4-orange.svg?style=flat-square)](https://files.minecraftforge.net)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square)](./LICENSE)

</div>

<div align="center">

[What it is](#what-it-is) · [Install](#install) · [Two upgrade tiers](#two-upgrade-tiers) · [Energy math](#energy-math) · [Large machines](#large-machines) · [Automation upgrades](#automation-upgrades) · [Config](#config) · [FAQ](#faq) · [Compatibility](#compatibility) · [For developers](#for-developers) · [Changelog](#changelog)

</div>

---

## What it is

Mekanism: Empowered and Mekanism Unleashed both rewrite the same part of Mekanism: how speed and energy upgrades translate into operations per tick, energy per operation and energy capacity. Each mod replaces the same method body, so installing both means one silently wins.

This mod merges the two into one jar, with one logic layer stacked on the other instead of overwriting it:

- single jar, single mod id (`mekanism_empowered_unleashed`), no separate core dependency;
- two more upgrade items on top of vanilla Speed and Energy: Empowered Speed and Empowered Energy;
- this branch targets Minecraft 1.20.1 + Forge 47.4+; for 1.21.1 see the [1.21.1 branch](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/tree/1.21.1).

## Install

| File | Required | Notes |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.20.1-20.1-*.jar` | yes | this mod |
| `Mekanism-1.20.1-10.4.16.*.jar` | yes | Mekanism itself |
| `kotlinforforge-4.12.0-all.jar` | yes | Kotlin runtime |
| Forge 47.4+ | yes | loader |

- Minecraft 1.20.1, Forge 47.4+, Java 17.
- **Do not install the original Mekanism: Empowered or Mekanism Unleashed alongside.** Both are fully included.
- `-sources.jar` and `-api.jar` are for developers and are not needed to play.

## Two upgrade tiers

| | Vanilla tier | Empowered tier |
|---|---|---|
| Items | Speed Upgrade, Energy Upgrade (from Mekanism) | Empowered Speed, Empowered Energy (added here) |
| Cap | `maxUpgrades`, default 16, range 8-32 | `maxEmpoweredUpgrades`, default 16, range 1-32 |
| When it applies | as soon as it is installed | only after the vanilla tier is full |
| Effect | Mekanism's own formulas | multiplies the vanilla result by `multiplier^(installed/cap)` |

Mekanism allows 8 Speed and 8 Energy upgrades by default. This mod raises the cap to `maxUpgrades` (default 16), so the 9th upgrade onwards is what this mod adds; the empowered tier unlocks only when the vanilla tier is full.

With [MekaJade Upgrades Fixed](https://github.com/zaixiayesheng/MekaJadeUpgradesFixed) installed, the Jade tooltip lists the upgrades inside the machine, including Empowered Speed and Empowered Energy.

With the default config (16 / 16 / 16 / 16), a normal machine gets:

| Metric | Relative to vanilla |
|---|---|
| Time per operation | x1/2,000 |
| Energy per tick | x8,944 |
| Energy per operation | x4.47 |
| Energy capacity | x2,000 |
| Uptime on a full buffer | x0.22 |

Raising `maxUpgrades` dilutes the empowered multiplier automatically:

| `maxUpgrades` | Vanilla tier (full) | Empowered tier (effective) | Combined |
|---|---|---|---|
| 8 | x10 | x20 | x200 |
| 16 (default) | x100 | x20 | x2,000 |
| 24 | x1,000 | x7.37 | x7,370 |
| 32 | x10,000 | x4.47 | x44,700 |

## Energy math

- **Vanilla tier**: `vanillaEnergyOffsetRate`, default 1.0 — vanilla behaviour: with equal speed and energy counts, energy per operation returns to the vanilla value.
- **Empowered tier**: `empoweredEnergyOffsetRate`, default 0.5 — only half of the empowered speed cost is cancelled; at full empowerment that leaves about 4.47x vanilla energy per operation.

| `empoweredEnergyOffsetRate` | Energy per tick | Energy per operation | Uptime |
|---|---|---|---|
| 1.0 | x2,000 | x1 | x1 |
| 0.5 (default) | x8,944 | x4.47 | x0.22 |
| 0.25 | x18,915 | x9.46 | x0.11 |
| 0.0 | x40,000 | x20 | x0.05 |

Installing speed without energy is expensive — that is Mekanism's own behaviour. Capacity is governed by energy upgrades alone (x2,000 at full vanilla energy).

## Large machines

For the six large machines added by Mekanism: MoreMachine 1.2.x — Large Rotary Condensentrator, Large Electrolytic Separator, Large Chemical Infuser, Large Solar Neutron Activator, Large Antiprotonic Nucleosynthesizer and Large Pigment Mixer. Older MoreMachine versions (1.1.x and before) do not have these machines.

### Speed

Large machines keep MoreMachine's own algorithm; nothing is converted:

```
operations/tick = 4 * vanillaSpeedUpgrades * 2^vanillaSpeedUpgrades
with empowered speed full: += 4 * vanillaSpeedUpgrades * 2 * 2^empoweredSpeedUpgrades
```

- 16 vanilla Speed upgrades: 4,194,304 operations per tick;
- adding 16 Empowered Speed: 12,582,912 per tick;
- 32 + 32 overflows int; the mod clamps it to about 1.07 billion operations per tick (1,073,741,696 for a 32-speed large machine).

### Energy

Large machines get a **price cap** that cannot be configured and applies only to `com.jerry.meklm` machines:

```
price cap = floor(2,147,483,647 / input tank size in mB) * (1 - offset progress)
offset progress = (1.0 * vanillaEnergy + 0.5 * empoweredEnergy) / (2 * maxUpgrades)
```

A machine can consume at most one tank per tick, so a full tank never costs more than 2,147,483,647 FE:

| Machine | Input tank | Per operation | Double-zero offsets (full tank) | Default offsets (full tank) |
|---|---|---|---|---|
| Large Rotary Condensentrator | 10,000,000 mB | 1 mB water | 2,140,000,000 FE | 530,000,000 FE |
| Large Electrolytic Separator | 24,000,000 mB | 2 mB water | 1,068,000,000 FE | 264,000,000 FE |
| Large Chemical Infuser | 5,000,000 mB | 1 mB | 2,145,000,000 FE | 535,000,000 FE |
| Large Solar Neutron Activator | 10,000,000 mB | 1 mB | 2,140,000,000 FE | 530,000,000 FE |
| Large Antiprotonic Nucleosynthesizer | 10,000 mB | 1 mB | 2,147,480,000 FE | 536,870,000 FE |
| Large Pigment Mixer | 1,000 mB | 1 mB | 2,147,483,000 FE | 536,870,000 FE |

(The last four rows follow the formula and were not measured individually; measured values are in the changelog.)

The Large Electrolytic Separator uses `FixedUsageEnergyContainer`, whose price bypasses the normal energy path; the mod hooks that path separately.

## Automation upgrades

| Upgrade | Max per machine | Effect |
|---|---|---|
| Auto Inserter | 1 | adds an "Inserter" tab; all six sides start disabled. Pulls from adjacent blocks: 8 items/t, 128 mB/t fluids, 128 mB/t chemicals, 128 FE/t |
| Fast Item Insert | 8 | shortens the auto-inserter delay: `21^((8-installed)/8)-1` ticks — 13 ticks with 1 installed, every tick when full |
| Fast Item Eject | 8 | shortens the output delay and raises the per-transfer amount (about x9 when full) |
| I/O Capacity | 8 | fluids/energy/chemicals x(1+32*progress), x33 when full; items x8^progress, x8 when full |
| Gauge Droppers (4 tiers) | — | hand-held chemical/fluid containers: left click pulls, right click pours |

| Dropper | Capacity | Transfer | Crafting |
|---|---|---|---|
| Basic | 48,000 mB | 768 mB/t | Basic alloy + Mekanism gauge dropper |
| Advanced | 80,000 mB | 1,280 mB/t | Infused alloy + Basic dropper |
| Elite | 112,000 mB | 1,792 mB/t | Elite alloy + Advanced dropper |
| Ultimate | 144,000 mB | 2,304 mB/t | Atomic alloy + Elite dropper |

## Config

File: `config/MekanismEmpowered Unleashed/general.toml` (server config; restart or `/reload` after editing).

| Key | Default | Range | Meaning |
|---|---|---|---|
| `maxUpgrades` | 16 | 8-32 | cap for vanilla Speed/Energy; also the unlock threshold of the empowered tier |
| `maxEmpoweredUpgrades` | 16 | 1-32 | cap for Empowered Speed/Energy; only splits the bonus into steps |
| `maxUpgradeMultiplier` | 20 | >=1 | empowered multiplier at full; diluted to `multiplier^(16/maxUpgrades)` when the cap exceeds 16 |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0-1.0 | how much vanilla energy upgrades cancel the speed cost |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0-1.0 | how much empowered energy upgrades cancel the empowered speed cost |
| `enchantableMekaGear` | false | — | allow enchanting Meka gear |
| `maxOperationsPerTick` | 128 | 1-10000 | catch-up operations per tick for Pump, Digital Miner, Fluidic Plenisher, Formulaic Assemblicator |
| `autoScaleOperationsPerTick` | true | — | let the previous value follow `maxUpgrades` |

Dropper values live in `config/MekanismEmpowered Unleashed/tiers.toml`.

## FAQ

**Empowered Speed does nothing.** The empowered tier requires the vanilla Speed upgrades to be full (16 by default).

**The machine got slower or stops after upgrading.** Energy per tick exceeds the buffer. Add energy upgrades or capacity.

**Large machine shows G-level energy use.** Large machines keep MoreMachine's native operation count; the price cap keeps a full tank at or below 2,147,483,647 FE.

**Pump / Digital Miner is not faster after full upgrades.** Those four machines have a catch-up cap (128 per tick by default).

**Can I install only one of the two original mods?** No. This mod contains both logics.

## Compatibility

| Target | Status |
|---|---|
| Mekanism 10.4.16.x | required |
| Mekanism Generators / Additions / Tools | supported, same formulas |
| [MekaJade Upgrades Fixed](https://github.com/zaixiayesheng/MekaJadeUpgradesFixed) (Jade tooltip) | optional integration: lists the installed upgrades, including Empowered Speed / Energy |
| MoreMachine 1.2.x large machines | adapted (see above) |
| Mekanism Extras / Evolved Mekanism | optional mixins; skipped when the target class is missing |
| Useless Mod | coexists |
| Original Empowered / Unleashed | **do not install** |

Take the empowered upgrades out of machines before uninstalling; they turn into unknown items afterwards.

## For developers

### Implementation

The 1.20.1 branch mirrors the 1.21.1 layout: Java mixins under `mixin/common` use `@ModifyExpressionValue` / `@Inject` at fixed points and delegate the math to `mixin_impl/*.kt`; Kotlin mixins under `mixin/unleashed` hook Mekanism's utility classes directly.

| Hook | File | Purpose |
|---|---|---|
| `MekanismUtils.getTicksD` / `getOperationsPerTick` / `getEnergyPerTick` / `getMaxEnergy` | `mixin/unleashed/MixinMekanismUtils.kt` | HEAD retake that adds the empowered tier |
| `TileEntityMekanism.recalculateUpgrades` speed branch | `mixin/common/tile/MixinModifyRecalculationTarget.java` | make Empowered Speed trigger the vanilla recalculation |
| `Math.pow(2, count)` in large-machine `recalculateUpgrades` | `mixin/common/tile/machine/MixinModifyRecalculationBaselineMaxOperations.java` | rewrite the MoreMachine baseline |
| `MachineEnergyContainer.updateEnergyPerTick` / `updateMaxEnergy` | `mixin/common/MixinMachineEnergyContainer.java` | let the empowered tier participate and clamp energy per tick to capacity |
| `FixedUsageEnergyContainer.updateEnergyPerTick` | `mixin/common/MixinFixedUsageEnergyContainer.java` | same for the electrolytic separator path |
| `Upgrade.getMax` | `mixin/unleashed/MixinUpgradeMax.kt` | replace the fixed 8 with the configured cap |

1.20.1 energy containers use `FloatingLong` (1.21.1 uses `long`); the compensation and clamp are implemented with `FloatingLong.create(...)` and `greaterThan`.

### Build

JDK 17 and network access to the Maven repositories:

```
gradlew build
```

Output in `build/libs/`: the plain jar is the release artifact; `-api.jar` and `-sources.jar` are for developers.

## Changelog

### 20.1-1.0.4

**Large-machine speed restored to native semantics**

- Operations per tick are back to MoreMachine's own algorithm: `4 * vanillaSpeed * 2^vanillaSpeed`, plus `4 * vanillaSpeed * 2 * 2^empoweredSpeed` when Empowered Speed is full.
- Reason: 20.1-1.0.2 converted large machines to the normal-machine formula (`multiplier^(totalSpeed/8)`), which dropped 16 vanilla Speed upgrades from 4,194,304 to 448 operations per tick; 20.1-1.0.3 made that conversion unconditional.
- 32 + 32 overflows int; the mod clamps it to about 1.07 billion operations per tick (1,073,741,696 for a 32-speed large machine).

**Large-machine energy cap**

- New price cap: `floor(2,147,483,647 / input tank) * (1 - offset progress)`, where offset progress is `(1.0 * vanillaEnergy + 0.5 * empoweredEnergy) / (2 * maxUpgrades)`.
- A full tank never exceeds 2,147,483,647 FE; with both offsets at 0 the rotary costs 10,000,000 x 214 = 2,140,000,000 FE per tank, about a quarter at the default 1.0/0.5.
- Applies only to `com.jerry.meklm.*`, has no config option, and does not change normal machines.

**Large Electrolytic Separator now receives the compensation**

- It uses `FixedUsageEnergyContainer`, whose `updateEnergyPerTick()` writes the price directly instead of going through `MekanismUtils.getEnergyPerTick()`.
- Before the fix: 400 FE per operation, one tank of water (24,000,000 mB) cost 3,000,000,000 FE. After: 89 FE per operation with both offsets at 0, 22 FE at the default.

**Unchanged**

- Integer headroom and the "energy per tick never exceeds the buffer" clamp remain.
- Normal machines get no compensation.

<details>
<summary><b>20.1-1.0.3 (reverted in 20.1-1.0.4)</b></summary>

- Large machines switched to the normal-machine formula `multiplier^(totalSpeed/8)`: 16 vanilla Speed upgrades fell from 4,194,304 to about 128 operations per tick.
- The conversion became unconditional, fixing the 20.1-1.0.2 regression where Empowered Speed made machines slower.
- **Reverted because** the lower operation count made large machines slower than the native design.

</details>

<details>
<summary><b>20.1-1.0.2 (partially reverted in 20.1-1.0.4)</b></summary>

- Fixed large machines stopping when fully upgraded: MoreMachine computed operations per tick as an int product that overflowed to a negative value, making `stored / energyPerTick` zero and idling the machine.
- Added the "energy per tick never exceeds the buffer" clamp.
- Covered `FixedUsageEnergyContainer`, used by the electrolytic separator.
- **Reverted because** the speed rewrite was reverted in 20.1-1.0.4; the clamp remains.

</details>

<details>
<summary><b>20.1-1.0.1</b></summary>

- Fixed Empowered Speed not being written to the mod's own NBT slice.
- Old saves migrate automatically; saves without a slice are no longer cleared.
- Fixed 1.20.1-only optional targets (`com.jerry.mekextras.*` to `com.jerry.mekanism_extras.*`, plus mekaf / mekmm base classes).
- Measured on a 1.20.1 dedicated server: 16 vanilla speed/energy plus 8 empowered speed/energy survive a restart.

</details>

<details>
<summary><b>20.1-1.0.0</b></summary>

- First 1.20.1 release: Forge 47 + Kotlin for Forge 4.x, single jar.
- Two-tier upgrade system (cap 8-32, default 16), six new upgrades, four dropper tiers.
- Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator can process several times per tick.
- Fixed empowered upgrade levels dropping from 16 to 8 after a reload (1.20.1 reads `maxStack` directly).
- Fixed upgrades requiring insertion order; fixed placed-block config NBT restore (core source set refmap missing from the jar).
- Fixed the creative tab title showing English; hardening and tooltip corrections.

</details>

## License and credits

- Based on [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered) (Copyright © 2025 Lapis, MIT) and Mekanism Unleashed (Copyright © 2024 WhitePhant0m, MIT)
- Merge and modifications: Copyright © 2026 zaixiayesheng, MIT License
- Full text in [LICENSE](./LICENSE)

## Feedback

Bug reports and suggestions: [GitHub Issues](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues).

Please include Minecraft and Forge versions, Mekanism and MoreMachine versions, this mod's version, `logs/latest.log`, and the machine and upgrade layout involved.
