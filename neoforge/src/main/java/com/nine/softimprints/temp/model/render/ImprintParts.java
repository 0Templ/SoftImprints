package com.nine.softimprints.temp.model.render;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class ImprintParts {

    private ImprintParts() {
    }

    public static BlockStateModelPart staticUpPart(
            List<BakedQuad> quads,
            TextureAtlasSprite particleSprite
    ) {
        return new StaticUpPart(quads, new Material.Baked(particleSprite, false), materialFlags(quads), true);
    }


    public static BlockStateModelPart withoutUpQuads(BlockStateModelPart original) {
        List<BakedQuad> upQuads = original.getQuads(Direction.UP);
        if (upQuads.isEmpty()) {
            // Still need to filter the null bucket, which can contain UP-tagged general quads.
            List<BakedQuad> all = original.getQuads(null);
            List<BakedQuad> filtered = filterOutUp(all);
            if (filtered == all) {
                return original;
            }
            return new FilteredPart(original, filtered);
        }
        List<BakedQuad> all = original.getQuads(null);
        List<BakedQuad> filtered = filterOutUp(all);
        return new FilteredPart(original, filtered);
    }

    private static List<BakedQuad> filterOutUp(List<BakedQuad> quads) {
        if (quads.isEmpty()) return quads;
        List<BakedQuad> out = null;
        for (int i = 0; i < quads.size(); i++) {
            BakedQuad q = quads.get(i);
            if (q.direction() == Direction.UP) {
                if (out == null) {
                    out = new ArrayList<>(quads.size() - 1);
                    for (int j = 0; j < i; j++) {
                        out.add(quads.get(j));
                    }
                }
                continue;
            }
            if (out != null) {
                out.add(q);
            }
        }
        return out == null ? quads : out;
    }

    private static int materialFlags(List<BakedQuad> quads) {
        int flags = 0;
        for (BakedQuad quad : quads) {
            flags |= quad.materialInfo().flags();
        }
        return flags;
    }

    private record FilteredPart(BlockStateModelPart delegate, List<BakedQuad> filteredAll)
            implements BlockStateModelPart {

        @Override
        public List<BakedQuad> getQuads(Direction side) {
            if (side == null) {
                return this.filteredAll;
            }
            if (side == Direction.UP) {
                return Collections.emptyList();
            }
            return this.delegate.getQuads(side);
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.delegate.useAmbientOcclusion();
        }

        @Override
        public Material.Baked particleMaterial() {
            return this.delegate.particleMaterial();
        }

        @Override
        public int materialFlags() {
            return this.delegate.materialFlags();
        }
    }

    private record StaticUpPart(
            List<BakedQuad> allQuads,
            Material.Baked particleMaterial,
            int materialFlags,
            boolean ambientOcclusionEnabled
    ) implements BlockStateModelPart {

        @Override
        public List<BakedQuad> getQuads(Direction side) {
            if (side == null || side == Direction.UP) {
                return this.allQuads;
            }
            return Collections.emptyList();
        }

        @Override
        public boolean useAmbientOcclusion() {
            return this.ambientOcclusionEnabled;
        }

    }
}
