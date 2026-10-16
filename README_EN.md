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

[What this is](#-what-this-is) · [Features](#-features) · [Installation](#-installation) · [Compatibility](#-compatibility) · [Configuration](#-configuration) · [Numbers](#-numbers) · [Differences from upstream](#-differences-from-upstream) · [Known issues](#-known-issues) · [Building](#-building) · [Changelog](#-changelog) · [License & credits](#-license--credits) · [Feedback](#-feedback)

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

## 🔧 Differences from upstream

| Problem | Resolution |
|---|---|
| Unleashed replaced all of `MekanismUtils` with `@Overwrite`, killing Empowered's point injections | The hub uses point injections with staggered targets instead of replacing the method |
| Energy upgrades beyond 8 not reducing consumption | Cap follows the `maxUpgrades` config |
| Tier-2 capacity exponent had an extra factor of 2 | Capacity now matches speed and energy per tick: ×40,000 → ×2,000 at full |
| Catch-up cap of the four machines hardcoded to 128 | Configurable `maxOperationsPerTick`, auto-following `maxUpgrades` by default |
| Tier-2 tooltips showed the full bonus for every single upgrade, 8× above the real effect | They now show `factor^(installed/cap)`; both the pump and the I/O capacity lines use the real formula |
| Tier-2 energy formula hardcoded to 8 | Normalized against the configured cap; how much energy it cancels is set by `empoweredEnergyOffsetRate` |
| Tier-2 factor ignored the vanilla cap | Auto-dilutes to `maxUpgradeMultiplier^(16/maxUpgrades)`; it only ever goes down |
| Both tier caps tied to a single knob | Split into `maxUpgrades` (tier 1 + unlock threshold) and `maxEmpoweredUpgrades` (tier 2) |
| Energy offset baked into the formulas, no way for packs to tune difficulty | New `vanillaEnergyOffsetRate` (1.0) and `empoweredEnergyOffsetRate` (0.5) |
| Config read during registry events crashed when it was not loaded yet | Config reads fall back to defaults instead of throwing |
| `MixinModifyRecalculationTarget$Energy` relied on a brittle bytecode match | Replaced with a `RETURN` injection replaying the virtual call; optional mod targets no longer have to match |
| `enchantableMekaGear` never worked on Meka Armor | Runtime injection into `ItemMekaSuitArmor` instead of a compile-time lambda index |
| On 1.20.1 the Empowered upgrade level dropped from 16 back to 8 when a save was reloaded | `Upgrade.getMax()` was rewritten, but 10.4 reads the `maxStack` field directly in `Upgrade.buildMap`; that field read now has a `require = 1` injection |
| Inserting a vanilla upgrade and then an Empowered one did nothing until a vanilla upgrade was pulled and reinserted | Vanilla `recalculateUpgrades` only knows SPEED/ENERGY and did not refresh the cache for Empowered upgrades; inserting or removing any upgrade now forces a recalculation |
| "Restore config NBT when a block is placed" never worked outside the dev environment | The core source set's refmap was not packaged into the jar; it is now included |
| The creative tab title was hardcoded English | It goes through the language file now |
| A `@Pseudo` target from an optional mod that did not match crashed the game at startup | Those injections use `require = 0`; both mixin configs still keep `defaultRequire` at 1 |
| Config saving was asynchronous and the side-config button only refreshed after leaving the screen | Saving is synchronous now; the button refreshes its state every frame from the current config and facing |
| The mod's own NBT section never activated on 1.20.1, so Empowered upgrades stayed mixed into the vanilla `upgrades` list | The save hook grabbed a local by a hardcoded name (`arg1`) while 10.4 calls it `nbtTags`, so it silently did nothing; the name is no longer hardcoded. The ordinal range was also off by one, which kept the first added upgrade (Empowered Speed) out of our tag forever |
| With the section working again, an old save would have had its Empowered upgrades treated as "someone else's" and dropped on load | When our tag is missing, the load path hands the list back untouched instead of cleaning it; an old save migrates into the new tag on its first load |

## ⚠️ Known issues

- **`Error loading class: com/jerry/mekextras/...` in the startup log**: that is a target probe kept for 1.21.1; the addon package on 1.20.1 is `com.jerry.mekanism_extras`, so the probe never matches anything. It is harmless.
- **The `configuration.*` language keys have no consumer yet**: they are kept so the language files stay shared with the 1.21.1 branch, and they are never shown on 1.20.1.
- **Meka gear needs a manual tag**: `enchantableMekaGear` allows enchanting, but you still have to add the items to the enchantable tags yourself.
- **The four machines' throughput is derived, not measured**: the real throughput of the Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator under `maxOperationsPerTick`, and the factors in the tables above, were never measured on a server — they come from the formulas.
- **Saves damaged by an older jar need one manual reinsert**: if you ran a build from before the fix, loading the world cut your Empowered upgrade counts to 8 and autosave wrote that back to disk, so you have to insert those upgrades again.

## 🛠️ Building

Requirements: **JDK 17** and network access to Maven repositories.

```
gradlew build
```

## 📜 Changelog

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
