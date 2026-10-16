# Mekanism: Empowered Unleashed

> A Mekanism addon. The conflict-free merge of **Mekanism: Empowered** and **Mekanism Unleashed**.
>
> 中文说明：[README.md](./README.md)

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](./LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://www.minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1-orange.svg)](https://neoforged.net)

---

## What this is

The two original mods conflict when installed together. Both patch the shared utility functions in `MekanismUtils` via Mixin, but Unleashed uses `@Overwrite` (whole method-body replacement) while Empowered uses targeted injectors. Once the overwrite lands, the original instructions are gone, and Empowered either crashes at startup or silently stops working.

This project merges them into **one mod**:

- Single jar, single mod id (`mekanism_empowered_unleashed`), no separate Core dependency.
- The entire codebase unified in Kotlin.
- Injection points redesigned so they never overlap; both layers' effects stack properly.

Targets vanilla Mekanism only (1.21.1 / 10.7.19.x).

## Features

- **16+16 two-tier upgrade system**: vanilla Speed/Energy upgrades (tier 1, default cap 16), plus Empowered Speed/Energy upgrades (tier 2, unlocked once tier 1 is maxed)
- **Separate caps per tier**: `maxUpgrades` (8–32, default 16) drives tier 1 and the tier-2 unlock threshold. `maxEmpoweredUpgrades` (1–32, default 16) only decides how many steps tier 2's total bonus is split into
- **Tier-2 factor auto-dilution**: `maxUpgradeMultiplier` (default 20) shrinks as `maxUpgrades` rises (effective value = `factor^(16/maxUpgrades)`, 4.47 at cap 32). Raising the vanilla cap therefore cannot blow up speed or storage
- **Energy-cap bug fixed**: original Unleashed capped energy upgrades' effect at 8 (32 upgrades consumed as much as 8); the cap now follows the configured maximum
- **Four machines support multiple operations per tick**: Electric Pump, Digital Miner, Fluidic Plenisher, Formulaic Assemblicator
- **Meka gear enchant toggle** (`enchantableMekaGear`, inherited from Unleashed, off by default)
- **Upstream integrations**: Mekanism Extras, More Machine, Evolved Mekanism, Mekanism Generators, etc.

## Installation

| File | Required | Notes |
|---|---|---|
| `MekanismEmpoweredUnleashed-1.21.1-21.1-1.0.5.jar` | ✅ | This mod (single file) |
| `kotlinforforge-5.12.0-all.jar` | ✅ | Kotlin runtime |
| Mekanism 1.21.1 (vanilla 10.7.19.x) | ✅ | Base dependency |

- Platform: NeoForge 21.1.x, Minecraft 1.21.1
- **Do not install the originals alongside this**: both `mekanism-unleashed` and `mekanism-empowered` are fully included here
- `-sources.jar` / `-api.jar` are developer artifacts, not required

## Configuration

Config file: `config/mekanism_empowered_unleashed-general.toml`

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

## Numbers

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

Speed upgrades raise the energy use per tick while Energy upgrades lower it again; the ratio between the two is the offset rate. Tier 1 keeps the vanilla 100% offset (`vanillaEnergyOffsetRate = 1.0`), so with as many Energy as Speed upgrades the energy per operation is back to vanilla. Tier 2 defaults to a 50% offset (`empoweredEnergyOffsetRate = 0.5`), so a fully stocked machine uses more energy per operation than vanilla.

The two offset rates control how much extra energy each tier's Energy upgrades cancel: 1.0 cancels everything, 0.0 nothing. Packs and servers can use them to tune difficulty directly:

| `empoweredEnergyOffsetRate` | Energy per tick | Energy per operation | Endurance |
|---|---|---|---|
| 1.0 (old behaviour) | ×2,000 | ×1 | ×1 |
| 0.5 (default) | ×8,944 | ×4.47 | ×0.22 |
| 0.25 | ×18,915 | ×9.46 | ×0.11 |
| 0.0 | ×40,000 | ×20 | ×0.05 |

`vanillaEnergyOffsetRate` does the same for tier 1 (default 1.0); lowering it makes every machine thirstier while it runs fast.

The capacity factor is tier 1 (×100) times tier 2 (×20), the same order as speed and energy per tick. With the default offset rates the energy use per tick is ×8,944, so a full buffer lasts about ×0.22 as long (it used to be ×1). Upgrades scale power, not endurance; add capacity upgrades if you want longer runtime.

The Electric Pump, Digital Miner, Fluidic Plenisher and Formulaic Assemblicator each run their own per-tick catch-up loop, bounded by `maxOperationsPerTick` (default 128). At ten-thousand-fold speed the theoretical demand is far beyond that line, which is why speed upgrades stop making them faster. With `autoScaleOperationsPerTick = true` (default) the line follows `maxUpgrades` (vanilla factor^(N/8): 128 at N=16, 1000 at N=24, 10000 at N=32), so raising `maxUpgrades` alone speeds those machines up too; turn the switch off to make it a hard ceiling, or raise `maxOperationsPerTick` directly.

## Differences from upstream (fixes)

| Problem | Resolution |
|---|---|
| `@Overwrite` conflicting with Empowered injectors | All replaced by `@ModifyArg` / `@ModifyReturnValue` with non-overlapping points |
| Energy upgrades beyond 8 not reducing consumption | Cap follows the `maxUpgrades` config |
| Tier-2 capacity exponent was squared (×400 instead of ×20 at default) | Normalized to the same order as speed / energy per tick; storage on a full build drops from ×40,000 to ×2,000, endurance ×1 → ×0.22 |
| Catch-up cap of the four machines hardcoded to 128 | Configurable `maxOperationsPerTick`, auto-following `maxUpgrades` by default |
| Tier-2 factor fixed at 20 regardless of the vanilla cap: ×200,000 total and 200,000× storage at cap 32 | Tier-2 factor auto-dilutes (`factor^(16/N)`) → ×44,700 at cap 32; default numbers unchanged |
| Both tier caps tied to a single knob | Split into `maxUpgrades` (tier 1 + unlock threshold) and `maxEmpoweredUpgrades` (tier 2) |
| `enchantableMekaGear` never worked on Meka Armor | Runtime injection into `ItemMekaSuitArmor` instead of a compile-time lambda index |
| Empowered upgrade tooltips showed wrong numbers (`2^n`, vanilla factor 10, "+100%/level") | Both tiers now show the real `factor^(installed/cap)` multiplier |
| Tier-2 energy formula hardcoded to 8 | Normalized against the configured cap; how much energy it cancels is set by `empoweredEnergyOffsetRate` |
| Energy offset baked into the formulas, no way for packs to tune difficulty | New `vanillaEnergyOffsetRate` (1.0) and `empoweredEnergyOffsetRate` (0.5) |
| Config reads during registry init crashing the game | All read sites now fall back to defaults before config load |
| Fragile `MixinModifyRecalculationTarget$Energy` bytecode pattern | Rewritten as RETURN injection + virtual re-dispatch, version-independent |
| Two mods, three files | Merged into one mod, one jar |

## Building

Requirements: **JDK 21** and network access to Maven repositories.

```
gradlew build
```

## License & credits

- Based on [Mekanism: Empowered](https://github.com/Lapis256/MekanismEmpowered) (Copyright © 2025 Lapis, MIT) and Mekanism Unleashed (Copyright © 2024 WhitePhant0m, MIT)
- Merge & modifications: Copyright © 2026 zaixiayesheng, MIT License
- Full license in [LICENSE](./LICENSE)
