# Soft Imprints

[![GitHub License](https://img.shields.io/github/license/0Templ/SoftImprints?style=for-the-badge)](LICENSE)
[![Modrinth Downloads](https://img.shields.io/modrinth/dt/snow-imprints?style=for-the-badge&logo=modrinth&label=Modrinth)](https://modrinth.com/mod/snow-imprints)
[![CurseForge Downloads](https://img.shields.io/curseforge/dt/1474300?style=for-the-badge&logo=curseforge&label=CurseForge)](https://www.curseforge.com/minecraft/mc-mods/snow-imprints)

Soft Imprints is a client-side Minecraft mod that adds dynamic surface imprints as entities move across supported blocks.

## Custom Profiles

Soft Imprints can be extended with JSON imprint profiles from resource packs or addon mods.

Place profile files at:

```text
assets/<namespace>/imprint_profiles/<profile_id>.json
```


## Example Profile

```json
{
  "layers": [
    {
      "value": 1,
      "enable": true,
      "expand": 0,
      "innerJitter": 0.0,
      "outerJitter": 0.0,
      "erosion": 0.0
    },
    {
      "value": 2,
      "enable": true,
      "expand": 2,
      "innerJitter": 0.0,
      "outerJitter": 0.0,
      "erosion": 0.0
    }
  ],
  "supportedBlocks": [
    "minecraft:iron_block"
  ],
  "surface": {
    "mode": "TOP",
    "zeroLayerSource": "SURFACE"
  },
  "textureSets": {
    "selected": "standard",
    "initLayer": "example:block/iron_block_imprint_0",
    "texturesByValue": {
      "standard": {
        "1": "example:block/iron_block_imprint_1",
        "2": "example:block/iron_block_imprint_2"
      },
      "other": {
        "1": "example:block/iron_block_alt_imprint_1",
        "2": "example:block/iron_block_alt_imprint_2"
      }
    }
  }
}
```

## Profile Fields

| Field | Description |
|---|---|
| `supportedBlocks` | Block IDs that can receive this imprint profile. |
| `layers` | Ordered imprint layer definitions. |
| `layers[].value` | Layer byte value. Must match keys in texture sets. Usually `1..127`. |
| `layers[].enable` | Enables or disables this layer. |
| `layers[].expand` | Expands this layer outward from the contact shape. |
| `layers[].innerJitter` | Adds random variation inside the imprint shape. |
| `layers[].outerJitter` | Adds random variation around the outer edge. |
| `layers[].erosion` | Removes random parts of the layer for a rougher shape. |
| `surface.mode` | Render mode. `TOP` replaces the top surface; `OVERLAY` draws above it. |
| `surface.zeroLayerSource` | Base layer source. `SURFACE` uses the block's real top texture; profile texture uses `textureSets.initLayer`. |
| `textureSets.selected` | Default texture set name. Must exist in `textureSets.texturesByValue`. |
| `textureSets.initLayer` | Base/profile zero-layer texture identifier. |
| `textureSets.texturesByValue` | Named texture sets. |
| `textureSets.texturesByValue.<set>.<value>` | Texture identifier for a specific layer value. |

