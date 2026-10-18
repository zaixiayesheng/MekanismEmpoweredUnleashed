<p align="center"><img src="images/logo.png" width="180" alt="Mekanism: Empowered Unleashed"></p>

<h1 align="center">Mekanism: Empowered Unleashed</h1>

<h3 align="center">通用机械：增强·解放</h3>

<div align="center">

A Mekanism addon. The conflict-free merge of **Mekanism: Empowered** and **Mekanism Unleashed**.

中文说明：[README.md](./README.md)

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

[What this is](#-what-this-is) · [Features](#-features) · [Installation](#-installation) · [Compatibility](#-compatibility) · [Configuration](#-configuration) · [Numbers](#-numbers) · [Design choices](#-design-choices) · [Building](#-building) · [Changelog](#-changelog) · [License & credits](#-license--credits) · [Feedback](#-feedback)

</div>

---

## 📖 What this is

This project merges the gameplay of Mekanism: Empowered and Mekanism Unleashed into one mod. Both of them patch the upgrade math in `MekanismUtils`; a method body can only keep one version, so installing both means only one layer's effect applies. This mod rearranges the two layers and ships them in a single jar.

This project merges them into **one mod**:

- Single jar, single mod id (`mekanism_empowered_unleashed`), no separate Core dependency.
- The entire codebase unified in Kotlin.
- Injection points redesigned so they never overlap; both layers' effects stack properly.

Targets vanilla Mekanism only (1.21.1 / 10.7.19.x).

## ✨ Features

- **16+16 two-tier upgrade system**: vanilla Speed/Energy upgrades (tier 1, default cap 16), plus Empowered Speed/Energy upgrades (tier 2, unlocked once tier 1 is maxed)
- **Separate caps per tier**: `maxUpgrades` (8–32, default 16) drives tier 1 and the tier-2 unlock threshold. `maxEmpoweredUpgrades` (1–32, default 16) only decides how many steps tier 2's total bonus is split into
- **Tier-2 factor auto-dilution**: `maxUpgradeMultiplier` (default 20) shrinks as `maxUpgrades` rises (effective value = `factor^(16/maxUpgrades)`, 4.47 at cap 32). Raising the vanilla cap therefore cannot blow up speed or storage
- **Energy-cap bug fixed**: original Unleashed capped energy upgrades' effect at 8 (32 upgrades consumed as much as 8); the cap now follows the configured maximum
- **Four machines support multiple operations per tick**: Electric Pump, Digital Miner, Fluidic Plenisher, Formulaic Assemblicator
- **Meka gear enchant toggle** (`enchantableMekaGear`, inherited from Unleashed, off by default)
- **Upstream integrations**: Mekanism Extras, More Machine, Evolved Mekanism, Mekanism Generators, etc.

## 📦 Installation

| File | Required | Notes |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.21.1-21.1-*.jar` | ✅ | This mod (single file) |
| `kotlinforforge-5.12.0-all.jar` | ✅ | Kotlin runtime |
| Mekanism 1.21.1 (vanilla 10.7.19.x) | ✅ | Base dependency |

- Platform: NeoForge 21.1.x, Minecraft 1.21.1
- **Do not install the originals alongside this**: both `mekanism-unleashed` and `mekanism-empowered` are fully included here
- `-sources.jar` / `-api.jar` are developer artifacts, not required

## 🤝 Compatibility

### UselessMod

Both mods can be installed together: Empowered upgrades, Fast Item Eject and the rest keep working, and UselessMod's own Alloy Furnace, Beef tools, Ore Generator and other content are unaffected.

Up to 1.0.3 this mod declared `useless_mod` as incompatible in its metadata, which made the game fail to start when both were installed; that declaration was removed in 1.0.4.

Which spots collided and how each was handled is documented in the source comments (`UselessModCompat`, and the mixin classes under `mixin/unleashed`).

## ⚙️ Configuration

Config file: `config/MekanismEmpowered Unleashed/general.toml`

| Option | Default | Range | Purpose |
|---|---|---|---|
| `maxUpgrades` | 16 | 8–32 | Cap of vanilla Speed/Energy upgrades; also the tier-2 unlock threshold (maxed vanilla count) |
| `maxEmpoweredUpgrades` | 16 | 1–32 | Cap of Empowered Speed/Energy upgrades. It only decides how many steps tier 2's total bonus is split into; the total at full is set by `maxUpgradeMultiplier` |
| `maxUpgradeMultiplier` | 20 | ≥1 | Tier-2 factor when fully stocked. Above `maxUpgrades = 16` it auto-dilutes to `factor^(16/maxUpgrades)` (7.37 at cap 24, 4.47 at cap 32) |
| `vanillaEnergyOffsetRate` | 1.0 | 0.0–1.0 | How much of the extra draw from Speed upgrades the vanilla Energy upgrades cancel. 1.0 is vanilla behaviour (equal counts = base energy per operation); lower it to make the line thirstier |
| `empoweredEnergyOffsetRate` | 0.5 | 0.0–1.0 | The same knob for the Empowered Energy line. The default cancels only half, so tier 2 is not energy-neutral; set it to 1.0 to restore energy per operation ×1 |
| `enchantableMekaGear` | false | — | Vanilla Meka gear cannot be enchanted; turning this on allows it, but you must add the items to enchantable tags yourself |
| `maxOperationsPerTick` | 128 | 1–10000 | Per-tick catch-up cap of the Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator. Speed upgrades past that point have no visible effect on those four machines; raising it costs server performance |
| `autoScaleOperationsPerTick` | true | — | Whether the cap above follows `maxUpgrades` automatically (it only ever rises). On by default, so tuning `maxUpgrades` alone is enough; turn it off to make `maxOperationsPerTick` a hard ceiling |

## 📊 Numbers

With default factors (vanilla 10, Empowered 20), a fully maxed machine (16 speed + 16 energy + 16 empowered speed + 16 empowered energy):

| Metric | Factor |
|---|---|
| Time per operation | ×1/2000 |
| Energy per tick | ×8,944 |
| Energy per operation | ×4.47 |
| Energy storage | ×2,000 |
| Energy endurance (full buffer) | ×0.22 |
| Throughput | ×2000 |

Tier 1 grows as `10^(N/8)` while tier 2 dilutes as `20^(16/N)`, so the total rises monotonically without exploding. Tuning the inserted count alone takes you from conservative to extreme:

| `maxUpgrades` | Tier 1 at full | Tier-2 effective factor | Total |
|---|---|---|---|
| 8 | ×10 | ×20 | ×200 |
| 16 (default) | ×100 | ×20 | ×2,000 |
| 24 | ×1,000 | ×7.37 | ×7,370 |
| 32 | ×10,000 | ×4.47 | ×44,700 |

`maxEmpoweredUpgrades` does not change those totals; it only changes how many steps tier 2 is split into (about +20% per step at 16, +10% at 32).

> The old behaviour gave ×200,000 at cap 32 with storage rising by the same 200,000×, which on top of addon machines like Mekanism Extras easily saturated `Long.MAX`. Cap 32 now gives ×44,700.

### How the energy draw is calculated

Speed upgrades raise the energy use per tick while Energy upgrades lower it again; the ratio between the two is the offset rate. Tier 1 keeps the vanilla 100% offset (`vanillaEnergyOffsetRate = 1.0`), so with as many Energy as Speed upgrades the energy per operation is back to vanilla. Tier 2 defaults to a 50% offset (`empoweredEnergyOffsetRate = 0.5`), so a fully stocked machine uses more energy per operation than vanilla.

The two offset rates control how much extra energy each tier's Energy upgrades cancel: 1.0 cancels everything, 0.0 nothing. Packs and servers can use them to tune difficulty directly:

| `empoweredEnergyOffsetRate` | Energy per tick | Energy per operation | Endurance |
|---|---|---|---|
| 1.0 (old behaviour) | ×2,000 | ×1 | ×1 |
| 0.5 (default) | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

`vanillaEnergyOffsetRate` does the same for tier 1 (default 1.0); lowering it makes every machine thirstier while it runs fast.

### Why capacity is ×2,000

The capacity factor is tier 1 (×100) times tier 2 (×20), the same order as speed and energy per tick. With the default offset rates the energy use per tick is ×8,944, so a full buffer lasts about ×0.22 as long (it used to be ×1). Upgrades scale power, not endurance; add capacity upgrades if you want longer runtime.

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

Requirements: **JDK 21** and network access to Maven repositories.

```
gradlew build
```

## 📜 Changelog

### 21.1-1.0.10

- Restored the original speed semantics for the Mekanism: MoreMachine large machines: the baseline is `2^vanilla speed` again, and an empowered speed upgrade only adds `2^(count + 1)` on top of it, exactly as in 21.1-1.0.7 and earlier. 21.1-1.0.9 had replaced it with the small-machine formula, which left 16 vanilla speed upgrades with 448 operations/tick instead of 4,194,304 — the large Rotary Condensentrator fell back to small-machine speed and the vanilla speed upgrades looked cancelled.
- Kept the integer overflow guard: `baseOperations × baseline` is still clamped so a high speed count can never wrap into a negative number and freeze the machine.
- Kept the 21.1-1.0.8 energy clamp (energy per tick can never exceed the buffer), so a shortage only slows the machine down instead of stalling it.
- Worth knowing: with the original semantics the large machines really do request huge operation counts, so when energy runs short the energy tab shows a number on the same scale as the buffer. That is Mekanism: MoreMachine's own design; lowering it means giving up speed.

### 21.1-1.0.9

- Fixed the speed conversion for the large machines from Mekanism: MoreMachine. Operations per tick now follow the same rule as the small machines (`multiplier^(total speed / 8)`) instead of the native `2^speed × baseOperations`. At 16 speed upgrades this is about 128 operations per tick instead of 65,536, so the machine no longer claims far more operations than its energy buffer can pay for, and the absurd energy figure on the tab (e.g. 2.14G FE/t) is gone.
- Fixed a regression in 21.1-1.0.8: the conversion only kicked in when an empowered speed upgrade was installed, so inserting empowered speed actually pushed operations per tick from hundreds of thousands down to a few thousand, and the machine got slower the more you upgraded it. The conversion is now always applied and operations rise monotonically with speed.
- The result is rounded up and keeps a safety margin below the integer limit, so filling 32 speed upgrades cannot overflow and stall the machine.
- Verified on the dev server: 16 vanilla speed + 16 vanilla energy + 16 empowered energy gives 128 operations per tick; adding 16 empowered speed raises it to 10,048.

### 21.1-1.0.8

- Adapted the large machines from Mekanism: MoreMachine (Large Chemical Infuser, Large Pigment Mixer, Large Rotary Condensentrator, Large Electrolytic Separator, Large Solar Neutron Activator): they compute operations per tick as an integer product `baseOperations × 2^vanilla speed`, which overflows to a negative number once speed gets high. Mekanism divides stored energy by that negative per-tick cost, gets 0, and force-pauses the machine with both "not enough energy" and "reduced rate" set — in game the machine simply stops after upgrades are maxed. The baseline now follows the same rule as small machines (`multiplier^(total speed / 8)`) with headroom against the integer limit.
- Added a guard so per-tick energy usage can never exceed the machine's energy capacity. The worst case is a slower rate instead of the stall caused by "stored energy ÷ per-tick energy" becoming 0. The Large Electrolytic Separator uses `FixedUsageEnergyContainer`, which bypasses the vanilla energy calculation, so it is covered too.
- Verified on a dev server with 16/16 and 32/32 upgrade sets (32 vanilla speed plus 32 empowered speed): recalculation on load no longer overflows, and the resulting operations per tick are positive and within range.

### 21.1-1.0.7

- Fixed Empowered Speed (`empowered_speed`) never reaching the mod's own NBT partition: the partition's ordinal range was off by one, leaving the first added upgrade outside it and stuck in the vanilla `upgrades` list. The range now starts at the first added upgrade itself.
- Old saves migrate automatically: on load, if the partition does not have that entry yet, it is picked up from the vanilla list before the list is cleaned, so no levels are lost; from then on it lives in the mod's own partition.
- Saves without this mod's partition at all are no longer cleaned up, so the amount cannot be deleted outright.
- Tested on a dedicated NeoForge 1.21.1 server: a fresh partition round-trips unchanged; an old save's `{type:7,amount:9}` becomes `empowered_speed: 9` in the partition; saves with no partition and saves with only the legacy `additional_upgrades` key both behave correctly; levels survive `save-all flush` plus a full restart.

### 21.1-1.0.6

- Fixed the Auto Inserter side-toggle network packet skipping validation: any client could flip the toggle on someone else's machine and write it into the save. It now goes through `IBlockSecurityUtils.canAccess` first, like Mekanism's own machine packets.
- Fixed Digital Miner catch-up mining being free work: each catch-up pass now runs a full vanilla tick, including the energy check and the vanilla delay, the same mechanism the Pump, Fluidic Plenisher and Formulaic Assemblicator already use.
- Fixed `Temp.inject` having no exception guard: one exception thrown during catch-up left that thread stuck "injecting", after which catch-up on the Pump, Fluidic Plenisher, Formulaic Assemblicator and Digital Miner all degraded to once per tick.
- Fixed config saving being asynchronous: changes could be lost when quitting right after editing settings in game; saving is now synchronous, with the three retries kept.
- Fixed the side-config button's enabled state being computed only when the screen opens: after toggling a switch on the side config page the button recoloured but stayed unclickable; it now refreshes every frame.
- Fixed item-form energy capacity display not updating: after removing or adding vanilla Energy upgrades the tooltip still showed the old buffer.
- Fixed the IO Capacity upgrade tooltip: the old text computed +3200% per level and showed +51200% at max, while the real factors are ×33 for fluid/energy/chemical and ×8 for items; it now shows two lines based on the installed ratio.
- Corrected the config comment claiming the max upgrade multiplier does not grow with the cap; the vanilla line itself still grows.
- The creative tab name is now a translatable string: a Chinese client shows 「通用机械：增强·解放」, where it used to be hardcoded English.
- Compatibility hardening: `@Pseudo` injections against 25 optional mod targets no longer crash at startup when the target bytecode does not match (`defaultRequire = 0`); injection points gained `ordinal`, full method descriptors and `require = 0`; one dead mixin that never applied was removed.

### 21.1-1.0.5

- New `maxEmpoweredUpgrades` (default 16, range 1–32) splits the tier-2 cap from tier 1; it only decides how many steps tier 2's total bonus is split into. `maxUpgrades` is still the tier-1 cap and the tier-2 unlock threshold.
- The tier-2 factor now auto-dilutes with the vanilla cap (`maxUpgradeMultiplier^(16/maxUpgrades)`, it only goes down). Cap 32 used to give ×200,000 total with storage up 200,000×, easily saturating `Long.MAX`; cap 32 is now ×44,700, the default 16 is unchanged, and the per-step gain shrinks with the cap (about +20.6%/step at 16, +9.7%/step at 32).
- Fixed an extra square in the tier-2 Energy upgrade capacity factor: full-build storage goes from ×40,000 to ×2,000, the same order as speed and energy per tick.
- New `maxOperationsPerTick` (default 128, range 1–10000) and `autoScaleOperationsPerTick` (default true): the per-tick catch-up cap of the Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator is no longer hardcoded to 128, and with auto-scaling on it follows `maxUpgrades` (128 at 16, 1000 at 24, 10000 at 32).
- New `vanillaEnergyOffsetRate` (default 1.0) and `empoweredEnergyOffsetRate` (default 0.5) control how much extra draw each tier's Energy upgrades cancel. By default tier 2 is no longer energy-neutral: at full it uses ×4.47 energy per operation with ×0.22 endurance; set 1.0 to return to the old behaviour, and packs can use it to lock difficulty.
- Fixed `enchantableMekaGear` doing nothing on Meka Armor: the old implementation injected the compile-time lambda of `MekanismArmorMaterials`, and the material is built during class init, when config is not loaded yet, so it could only fall back to false. It now injects `ItemMekaSuitArmor` at runtime and no longer depends on Mekanism's lambda index.
- Fixed Empowered Speed/Energy upgrade tooltips showing numbers that were not real: Empowered Speed showed `2^installed`, Empowered Energy used the vanilla factor 10, and the Pump text was hardcoded to +100%/level; both now show `factor^(installed/cap)`.

<details>
<summary>Older releases</summary>

### 21.1-1.0.4

- Removed the UselessMod incompatibility declaration, so both mods can be installed together
- Fixed Empowered upgrades breaking when both were installed; Fast Item Eject works again

### 21.1-1.0.3

- Removed the embedded legacy-id compatibility submodule (bridge); the main jar is back to a single mod id
- MekaJade's upgrade icons are now adapted by MekaJade Upgrades Fixed directly, so the built-in bridge is no longer needed

### 21.1-1.0.2

- Fixed Productive Bees Genesis misdetecting this mod as an unsupported dependency and erroring at startup

### 21.1-1.0.1

- Fixed Empowered upgrades only working when inserted in order; any order now takes effect immediately.
- Fixed the Pump, Fluidic Plenisher, Formulaic Assemblicator and Digital Miner still doing one operation per tick at full Speed upgrades; they can now run several operations per tick, up to 128.
- Fixed MekaJadeUpgrades displaying Empowered upgrades incorrectly.

</details>

## ⚖️ License & credits

- Based on [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered) (Copyright © 2025 Lapis, MIT) and Mekanism Unleashed (Copyright © 2024 WhitePhant0m, MIT)
- Merge & modifications: Copyright © 2026 zaixiayesheng, MIT License
- Full license in [LICENSE](./LICENSE)

## 🐛 Feedback

Found a bug or have a suggestion? Open an [issue on GitHub](https://github.com/zaixiayesheng/MekanismEmpoweredUnleashed/issues).

Please include your game version, your Mekanism version and `logs/latest.log`.
