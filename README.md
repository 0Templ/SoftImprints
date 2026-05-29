# Soft Imprints

[![GitHub License](https://img.shields.io/github/license/0Templ/SoftImprints?style=for-the-badge)](LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/snow-imprints?style=for-the-badge&logo=modrinth&label=Modrinth)](https://modrinth.com/mod/snow-imprints)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1474300?style=for-the-badge&logo=curseforge&label=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/snow-imprints)

Soft Imprints is a client-side Minecraft mod that leaves dynamic imprints on supported surfaces as entities move across them.

For screenshots, supported versions, and downloads, see the [Modrinth](https://modrinth.com/mod/snow-imprints) or [CurseForge](https://www.curseforge.com/minecraft/mc-mods/snow-imprints) page.

## Contents

- [How Profiles Work](#how-profiles-work)
- [Example Profile](#example-profile)
- [Profile Fields](#profile-fields)
- [Translations](#translations)
- [Schema Versions & Migrations](#schema-versions--migrations)

## How Profiles Work

Profiles tell Soft Imprints which blocks can receive imprints, how those imprints are shaped, and which textures should be used. They can be shipped by a resource pack or by an addon mod.

Place profile files at:

```text
assets/<namespace>/imprint_profiles/<profile_id>.json
```

Texture identifiers in JSON use the normal resource-location form, **without** the `textures/` prefix and **without** the `.png` extension (e.g. `mud_imprints:block/imprints/mud/mud_base`).

Imprint textures must live under the **block** path (`textures/block/...`), because Soft Imprints reads them from the block texture atlas. Textures placed elsewhere will not be picked up.

## Example Profile

This profile adds imprints for `minecraft:mud`, with a `standard` and a `wet` texture set across three layers.

```json
{
  "version": 2,
  "priority": 0,
  "resolution": {
    "map_size": 16
  },
  "surface": {
    "mode": "repaint",
    "zero_layer_source": "surface"
  },
  "supported_blocks": [
    "minecraft:mud"
  ],
  "preview": {
    "base": "mud_imprints:block/imprints/mud/mud_base",
    "icon": "mud_imprints:block/imprints/mud/mud_icon"
  },
  "texture_sets": {
    "selected": "standard",
    "zero_layer": "mud_imprints:block/imprints/mud/mud_base",
    "textures_by_value": {
      "standard": {
        "1": "mud_imprints:block/imprints/mud/sets/standard_mud_layer_1",
        "2": "mud_imprints:block/imprints/mud/sets/standard_mud_layer_2"
      },
      "wet": {
        "1": "mud_imprints:block/imprints/mud/sets/wet_mud_layer_1",
        "2": "mud_imprints:block/imprints/mud/sets/wet_mud_layer_2"
      }
    }
  },
  "layers": [
    {
      "value": 1,
      "enable": true,
      "expand": 0,
      "inner_jitter": 0.0,
      "outer_jitter": 0.15,
      "erosion": 0.2
    },
    {
      "value": 2,
      "enable": true,
      "expand": 1,
      "inner_jitter": 0.2,
      "outer_jitter": 0.55,
      "erosion": 0.35
    }
  ]
}
```

## Json-Profile Fields

### Top level

| Field | Required | Description                                                                                                                            |
|---|:---:|----------------------------------------------------------------------------------------------------------------------------------------|
| `version` | Recommended | Profile schema version. Use `2` for new profiles. If missing, the loader treats the file as an older profile and migrates it in memory |
| `priority` | No | Resolves conflicts when several profiles support the same block. Higher priority wins. Defaults to `0`                                 |
| `supported_blocks` | No | Blocks that can receive this profile. Empty or missing keeps the profile available but does not assign it to blocks by default         |
| `resolution.map_size` | No | Internal mask size per block. Defaults to `16`. Intended to match the surface texture resolution — use a higher value for textures larger than 16x16, a lower one for smaller. |

### `surface`

| Field | Required | Description                                                                                              |
|---|:---:|----------------------------------------------------------------------------------------------------------|
| `surface.mode` | No | `repaint` redraws the top surface. `overlay` keeps the block model and draws imprints slightly above it. |
| `surface.zero_layer_source` | No | `surface` uses the block's current top texture as the base. `profile` uses `texture_sets.zero_layer`     |

### `preview`

| Field | Required | Description |
|---|:---:|---|
| `preview.base` | No | Base texture for the config preview. Falls back to `texture_sets.zero_layer`. |
| `preview.icon` | No | Icon for profile selectors. Falls back to `texture_sets.zero_layer`. |

### `texture_sets`

| Field | Required | Description |
|---|:---:|---|
| `texture_sets` | Yes | Texture variants for the profile. |
| `texture_sets.selected` | Yes | Default texture set ID. Must exist in `texture_sets.textures_by_value`. |
| `texture_sets.zero_layer` | Yes | Base/profile zero-layer texture. |
| `texture_sets.textures_by_value` | Yes | Named texture sets. Each set maps layer values to texture identifiers. |

### `layers[]`

Shape layers. Each layer uses one value from the active texture set.

| Field | Required | Description |
|---|:---:|---|
| `layers[].value` | Yes | Layer byte value. Should match a key in every texture set that needs to render this layer. Usually `1..127`. |
| `layers[].enable` | Yes | Turns this layer on or off. |
| `layers[].expand` | Yes | Expands the layer outward from the contact shape. |
| `layers[].inner_jitter` | Yes | Adds variation inside the imprint. |
| `layers[].outer_jitter` | Yes | Adds variation around the outer edge. |
| `layers[].erosion` | Yes | Removes random parts of the layer for a rougher imprint. |

## Translations

Profile names, texture set names, and pack names can be translated through base lang.json files:

```json
{
  "imprint_pack.mud_imprints": "Mud Imprints",
  "imprint_profile.mud_imprints.mud": "Mud",
  "imprint_profile.mud_imprints.mud.standard": "Standard",
  "imprint_profile.mud_imprints.mud.wet": "Wet"
}
```

The translation keys should follow the pattern:

| Key | Translates |
|---|---|
| `imprint_pack.<namespace>` | The pack/addon display name. |
| `imprint_profile.<namespace>.<profile_id>` | The profile display name. |
| `imprint_profile.<namespace>.<profile_id>.<set_id>` | A texture set display name. |

## Schema Versions & Migrations

Soft Imprints includes built-in migrations for older profiles. They are applied **in memory** while profiles load.

- Profiles **older** than the current schema (`2`) are migrated automatically at load time.
- Profiles with a schema version **newer** than the installed mod supports are rejected by the current loader.

