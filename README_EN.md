<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">Mekanism: Empowered Unleashed</h1>

<h3 align="center">通用机械：增强·解放</h3>

<div align="center">

A Mekanism addon. The conflict-free merge of **Mekanism: Empowered** and **Mekanism Unleashed**.

中文说明：[README.md](./README.md)

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

[What this is](#-what-this-is) · [Features](#-features) · [Installation](#-installation) · [Compatibility](#-compatibility) · [Configuration](#-configuration) · [Numbers](#-numbers) · [Design choices](#-design-choices) · [Known issues](#-known-issues) · [Building](#-building) · [Changelog](#-changelog) · [License & credits](#-license--credits) · [Feedback](#-feedback)

</div>

---

## 📖 What this is

The two mods cannot be installed together. Both of them use Mixins on `MekanismUtils`, the class that works out how strong an upgrade is. Unleashed replaces the whole method body with `@Overwrite`, Empowered injects at specific points; once the overwrite wins, the vanilla instructions are gone and Empowered's injections either crash at startup or quietly do nothing.

This project merges them into one mod:

- A single jar with a single mod id (`mekanism_empowered_unleashed`); the separate Core mod Empowered used to need is now part of the main jar
- All code unified in Kotlin
- The injection points are laid out so both tiers multiply in order instead of overriding each other

This branch targets Minecraft 1.20.1 with Forge 47.4.0, and only supports vanilla Mekanism (1.20.1 / 10.4.16.x).

## ✨ Features

- **16+16 two-tier upgrade system**: vanilla Speed/Energy upgrades (tier 1, cap 16 by default) plus Empowered Speed/Energy upgrades (tier 2, unlocked once tier 1 is full)
- **Separate caps per tier**: `maxUpgrades` (8–32, default 16) drives tier 1 and the tier-2 unlock threshold. `maxEmpoweredUpgrades` (1–32, default 16) only decides how many steps tier 2's total bonus is split into
- **Tier-2 factor auto-dilution**: `maxUpgradeMultiplier` shrinks as `maxUpgrades` rises (effective value = `factor^(16/maxUpgrades)`, 4.47 at cap 32). Raising the vanilla cap therefore cannot blow up speed or storage
- **The energy cap is fixed**: the old formula stopped Energy upgrades from mattering past 8, so 32 upgrades drew as much power as 8. The cap now follows `maxUpgrades`
- **Four machines can run multiple operations per tick**: Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator keep getting faster past one operation per tick, up to a configurable cap
- **Auto-insert and capacity upgrades**: fast item insert/eject, I/O capacity, and the Auto Inserter upgrade (with its own remembered window position and rate settings)
- **Four dropper tiers**: basic, advanced, elite and ultimate gauge droppers, with rates and capacities in `tiers.toml`
- **Meka gear enchant toggle** (`enchantableMekaGear`, inherited from Unleashed, off by default)
- **Upstream addon support**: machines from Mekanism Extras, Evolved Mekanism, Mekanism Elements, More Machine and Evolved Extras are covered by the Empowered upgrades and the per-tick catch-up loop

## 📦 Installation

| File | Required | Notes |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.20.1-20.1-1.0.0.jar` | ✅ | This mod; it is the only file you need |
| `kotlinforforge-4.12.0-all.jar` | ✅ | Kotlin runtime, **the mod refuses to load without it** |
| Mekanism 1.20.1-10.4.16.80 (10.4 line) | ✅ | Required dependency |

- Platform: Minecraft 1.20.1 with Forge 47.4.0+
- **Kotlin for Forge is a hard requirement**: it is the one dependency whose absence makes the mod refuse to load, so make sure it is in `mods` first
- **Do not install the originals alongside it**: the merged mod already contains everything from `mekanism-empowered` (including its Core) and `mekanism-unleashed`
- `-sources.jar` and `-api.jar` are for developers and do not need to be installed

## 🤝 Compatibility

### Optional mods

| Mod | Minimum version |
|---|---|
| Mekanism Extras | 1.5.0 |
| Evolved Mekanism | 1.2.1 |
| Mekanism Elements | 2.3 |
| More Machine | 1.2.1 |
| Evolved Extras | 1.4.1 |

These are optional; missing any of them still starts fine. With them installed, the machines they add are covered by the Empowered upgrades and the per-tick catch-up loop as well.

### UselessMod

UselessMod also overwrites the two `UpgradeUtils` methods that print the upgrade stacking tooltips, which is exactly what this mod does. This mod gives those two methods a higher mixin priority (2000), so when both are present this mod wins and the two can be installed together.

### Declared incompatible

These are declared as incompatible in the metadata, and installing them makes the game refuse to start:

- `mekanism_unleashed`: its features are part of this mod now
- `mekanismtweaks`, `mekanismupgradesreborn`: they change the same upgrade maths

## ⚙️ Configuration

Config files: `config/MekanismEmpowered Unleashed/general.toml` (upgrades and machines) and `tiers.toml` (gauge droppers).

| Option | Default | Range | Purpose |
|---|---|---|---|
| `maxUpgrades` | 16 | 8–32 | Cap of vanilla Speed/Energy upgrades; also the tier-2 unlock threshold (tier 2 unlocks once this many are installed) |
| `maxEmpoweredUpgrades` | 16 | 1–32 | Cap of Empowered Speed/Energy upgrades. It only decides how many steps tier 2's total bonus is split into; the total at full is set by `maxUpgradeMultiplier` |
| `maxUpgradeMultiplier` | 20 | ≥1 | Tier-2 factor when fully stocked. Above `maxUpgrades = 16` it auto-dilutes to `factor^(16/maxUpgrades)` (7.37 at cap 24, 4.47 at cap 32) |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0–1.0 | How much of the extra draw vanilla Energy upgrades cancel. At 1.0, as many Energy as Speed upgrades puts energy per operation back to vanilla |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0–1.0 | The same knob for the Empowered Energy line. The default cancels only half, so tier 2 is not energy-neutral; set it to 1.0 to restore energy per operation ×1 |
| `enchantableMekaGear` | false | — | Vanilla Meka gear cannot be enchanted; turning this on allows it, but you must add the items to enchantable tags yourself |
| `maxOperationsPerTick` | 128 | 1–10000 | Per-tick catch-up cap of the Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator. Speed upgrades past that point have no visible effect on those four machines; raising it costs server performance |
| `autoScaleOperationsPerTick` | true | — | Whether the cap above follows `maxUpgrades` automatically (it only ever rises). On by default, so tuning `maxUpgrades` alone is enough; turn it off to make `maxOperationsPerTick` a hard ceiling |

The gauge droppers live in their own file, `tiers.toml`:

| Dropper | Default rate | Default capacity |
|---|---|---|
| Basic | 768 mB/t | 48,000 mB |
| Advanced | 1,280 mB/t | 80,000 mB |
| Elite | 1,792 mB/t | 112,000 mB |
| Ultimate | 2,304 mB/t | 144,000 mB |

## 📊 Numbers

With the default config (`maxUpgrades=16`, `maxEmpoweredUpgrades=16`, vanilla factor 10, Empowered factor 20) and 16 Speed + 16 Energy + 16 Empowered Speed + 16 Empowered Energy installed:

| Metric | Factor |
|---|---|
| Time per operation | ×1/2000 |
| Energy per tick | ×8,944 |
| Energy per operation | ×4.47 |
| Energy storage | ×2,000 |
| Endurance | ×0.22 |
| Processing speed | ×2000 |

Tier 1 grows as `10^(N/8)` while tier 2 dilutes as `20^(16/N)`, so the total rises with `maxUpgrades` without running away:

| `maxUpgrades` | Tier 1 at full | Tier-2 effective factor | Total |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16 (default) | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

`maxEmpoweredUpgrades` does not change those totals; it only changes how many steps tier 2 is split into (about +20% per step at 16, +10% at 32).

> The old build tied both caps to one knob: at `maxUpgrades=32` the total ran up to ×200,000 with storage up 200,000×, easily saturating `Long.MAX`. Cap 32 is now ×44,700.

### How the energy draw is calculated

Speed upgrades raise the energy use per tick while Energy upgrades lower it again; the ratio between the two is the offset rate. Tier 1 keeps the vanilla 100% offset (`vanillaEnergyOffsetRate = 1.0`), so with as many Energy as Speed upgrades the energy per operation is back to vanilla. Tier 2 defaults to a 50% offset (`empoweredEnergyOffsetRate = 0.5`), so a fully stocked machine uses more energy per operation than vanilla. Stacking Speed without Energy makes the draw explode — that part is vanilla behaviour.

| `empoweredEnergyOffsetRate` | Energy per tick | Energy per operation | Endurance |
|---|---|---|---|
| 1.0 | ×2,000 | ×1 | ×1 |
| 0.5 (default) | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

### Why capacity is ×2,000

Capacity scales on the same order as speed and energy per tick: ×100 from tier 1 times ×20 from tier 2 gives ×2,000. At the default offset rate the energy per tick is ×8,944, so endurance drops to ×0.22 (×1 if you set the offset rate to 1.0). Upgrades amplify power; if you want a machine to run longer, add capacity upgrades.

### The four machines have a second cap

The Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator each run their own per-tick catch-up loop, bounded by `maxOperationsPerTick` (default 128). At ten-thousand-fold speed the theoretical demand is far beyond that line, which is why speed upgrades stop making them faster. With `autoScaleOperationsPerTick = true` (default) the line follows `maxUpgrades` (vanilla factor^(N/8): 128 at N=16, 1000 at N=24, 10000 at N=32), so raising `maxUpgrades` alone speeds those machines up too; turn the switch off to make it a hard ceiling, or raise `maxOperationsPerTick` directly.

## 🔧 Design choices

| Concern | How this mod handles it |
|---|---|
| Injection | Targeted `@ModifyArg` / `@ModifyReturnValue`, with the two layers'' points arranged so they never overlap |
| Energy upgrade offset | Applied within the range allowed by `maxUpgrades` |
| Tier-2 capacity | Same order as speed and energy per tick (x2,000 on a full build at defaults) |
| Catch-up cap | The four machines use `maxOperationsPerTick`, following `maxUpgrades` by default |
| Upgrade tooltips | Show the real `factor^(installed/cap)` multiplier |
| Tier-2 energy formula | Normalized against the configured cap; `empoweredEnergyOffsetRate` sets how much it cancels |
| Tier-2 factor dilution | Auto-dilutes as `factor^(16/N)` with `maxUpgrades` |
| Tier caps | Split into `maxUpgrades` (tier 1 + unlock) and `maxEmpoweredUpgrades` (tier 2) |
| Difficulty tuning | `vanillaEnergyOffsetRate` (1.0) and `empoweredEnergyOffsetRate` (0.5) |
| Config reads | Every read site falls back to defaults before config load |
| Injection stability | `MixinModifyRecalculationTarget$Energy` uses RETURN injection + virtual re-dispatch, independent of bytecode shape |
| `enchantableMekaGear` | Runtime injection into `ItemMekaSuitArmor`, no lambda index |
| Packaging | One mod, one jar |
## 🛠️ Building

Requirements: **JDK 17** and network access to Maven repositories.

```
gradlew build
```

## 📜 Changelog

### 20.1-1.0.4

- Restored the vanilla speed semantics for the large machines (Mekanism: MoreMachine): the baseline is `2^vanilla speed` again and the Empowered Speed upgrade only stacks `2^(count + 1)` on top of it, matching 1.0.7 and earlier. The previous build switched them to the small-machine formula, which dropped 16 vanilla speed upgrades to 448 operations/tick (vanilla: 4,194,304) and made the large rotary condensentrator as slow as the small one.
- Large-machine energy compensation is now anchored per machine, on that machine's own input tank: price = `2,147,483,647 / full-tank operations / (1 - offset progress)`. Full-tank operations come from each machine's input tank (rotary 10,000,000, electrolytic separator 24,000,000, chemical infuser 5,000,000, solar neutron activator 10,000,000, antiprotonic nucleosynthesizer 10,000, pigment mixer 1,000), so with both offset rates at 0 a full-tank run costs exactly 2.14G FE/t; at the default 1.0/0.5 it is about a quarter of that.
- Fixed the large electrolytic separator missing out on the compensation: it uses `FixedUsageEnergyContainer`, whose `updateEnergyPerTick` writes the price directly and never goes through `MekanismUtils.getEnergyPerTick`, so the compensation had no effect on it (measured: 400 FE per operation, one tank of water drained 3G FE). Both the compensation and the storage clamp now hook that path too.
- Integer protection and the "energy per tick never exceeds the buffer" clamp are unchanged; the compensation only applies to `com.jerry.meklm` large machines, has no config option and does not make power free.

### 20.1-1.0.3

- Fixed the speed conversion for the large machines from Mekanism: MoreMachine. Operations per tick now follow the same rule as the small machines (`multiplier^(total speed / 8)`) instead of the native `2^speed × baseOperations`. At 16 speed upgrades this is about 128 operations per tick instead of 65,536, so the machine no longer claims far more operations than its energy buffer can pay for, and the absurd energy figure on the tab (e.g. 2.14G FE/t) is gone.
- Fixed a regression in 20.1-1.0.2: the conversion only kicked in when an empowered speed upgrade was installed, so inserting empowered speed actually pushed operations per tick from hundreds of thousands down to a few thousand, and the machine got slower the more you upgraded it. The conversion is now always applied and operations rise monotonically with speed.
- The result is rounded up and keeps a safety margin below the integer limit, so filling 32 speed upgrades cannot overflow and stall the machine.
- Verified on the dev server: 16 vanilla speed + 16 vanilla energy gives 128 operations per tick; adding 16 empowered speed raises it to 10,048.

### 20.1-1.0.2

- The large machines from Mekanism: More Machine are handled on their own terms: their per-tick operation count was folded into a power of two and multiplied together, so once the upgrades piled up it overflowed into a negative number and the machine declared "not enough energy" and stalled. It is now computed the same way the small machines compute theirs, so 16/16, 32/32 and even fully maxed Empowered upgrades only ever brush the int bound
- Energy per tick can no longer exceed the energy capacity: Mekanism divides stored energy by the per-tick cost to decide how many operations fit in a tick, and a zero result raises "not enough energy" together with "not enough energy for the maximum speed", which forces the machine to stop. Clamping the cost means the worst case is a slower machine instead of a dead one
- Fixed-usage energy containers (what the large electrolytic separator uses) get the same clamp: they override the per-tick calculation themselves, so `MekanismUtils` never sees them and neither our scaling nor the cap used to apply

### 20.1-1.0.1

- Upgrades now really go into the mod's own NBT section: the save hook looked the local variable up by name (`arg1`), while Mekanism 10.4 calls it `nbtTags`, so it silently never applied and the added upgrades stayed in the vanilla list
- Fixed an off-by-one in the section's ordinal range: Empowered Speed (ordinal 7) was excluded, so it could never leave the vanilla list
- Old worlds migrate: when a save has no section of its own the vanilla list is no longer cleaned, and the added upgrades move into the new section on the first load instead of being deleted as "another mod's upgrades"
- Optional-mod targets now use the real 1.20.1 package `com.jerry.mekanism_extras` (the old `com.jerry.mekextras` from 1.21.1 never matched), plus the mekaf / mekmm integration base classes

### 20.1-1.0.0

The first release on 1.20.1: Empowered and Unleashed merged into one mod, with the fixes proven on the 1.21.1 branch.

- Merged into a single jar with a single mod id (`mekanism_empowered_unleashed`); Empowered's separate Core mod is part of the main jar and all code is Kotlin
- The upgrade hub uses point injections, so `MekanismUtils` is no longer replaced wholesale with `@Overwrite`
- Empowered upgrade levels no longer drop from 16 to 8 when a save is loaded: the field read in `Upgrade.buildMap` is covered by the rewrite now
- Insertion-order bug fixed: the cache recalculates as soon as an Empowered upgrade is inserted, so nothing has to be pulled and reinserted
- "Restore config NBT when a block is placed" works outside the dev environment again: the core source set's refmap is packaged into the jar
- The creative tab title goes through the language file instead of being hardcoded English
- `@Pseudo` targets from optional mods now use `require = 0`, so a bytecode mismatch no longer crashes the game at startup
- Config saving is synchronous; the side-config button refreshes from the current config every frame
- Tier-2 tooltips use the real formula, matching the implementation for both the pump and I/O capacity
- Fixes carried over from 1.21.1: Energy upgrades past 8 not reducing consumption, the extra square in tier-2 capacity, the hardcoded 128 catch-up cap, both caps tied to one knob, the hardcoded energy offset, and `enchantableMekaGear` doing nothing on Meka Armor

## ⚖️ License & credits

- Based on [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered) (Copyright © 2025 Lapis, MIT) and Mekanism Unleashed (Copyright © 2024 WhitePhant0m, MIT)
- Merge & modifications: Copyright © 2026 zaixiayesheng, MIT License
- Full license text: [LICENSE](./LICENSE)

## 🐛 Feedback

Found a bug or have a suggestion? Open an [issue on GitHub](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues).
