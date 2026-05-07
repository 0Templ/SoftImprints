package com.nine.softimprints.model.render;

import com.nine.softimprints.client.core.map.ImprintStrip;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadAtlas;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class EmitHelper {

    public static void emitStrip(
            QuadEmitter emitter,
            TextureAtlasSprite sprite,
            ImprintStrip strip,
            int mapSize,
            float depth,
            int bakeFlags
    ) {
        float x0 = strip.x0() / (float) mapSize;
        float x1 = strip.x1() / (float) mapSize;
        float y0 = 1.0F - ((strip.y0() + 1) / (float) mapSize);
        float y1 = 1.0F - (strip.y0() / (float) mapSize);

        float u0 = strip.x0() / (float) mapSize;
        float u1 = strip.x1() / (float) mapSize;
        float v0 = strip.y0() / (float) mapSize;
        float v1 = (strip.y0() + 1) / (float) mapSize;

        setUvRotated(emitter, sprite, 0, u0, v0, bakeFlags);
        setUvRotated(emitter, sprite, 1, u0, v1, bakeFlags);
        setUvRotated(emitter, sprite, 2, u1, v1, bakeFlags);
        setUvRotated(emitter, sprite, 3, u1, v0, bakeFlags);

        emitter.square(Direction.UP, x0, y0, x1, y1, depth);
        emitter.atlas(QuadAtlas.BLOCK);

        emitter.color(-1, -1, -1, -1);
        emitter.diffuseShade(true);
        emitter.ambientOcclusion(TriState.DEFAULT);

        emitter.emit();

    }

    private static void setUvRotated(
            QuadEmitter emitter,
            TextureAtlasSprite sprite,
            int vertex,
            float u,
            float v,
            int rotation
    ) {
        float ru;
        float rv;

        switch (rotation & 3) {
            case 1 -> {
                ru = 1.0F - v;
                rv = u;
            }
            case 2 -> {
                ru = 1.0F - u;
                rv = 1.0F - v;
            }
            case 3 -> {
                ru = v;
                rv = 1.0F - u;
            }
            default -> {
                ru = u;
                rv = v;
            }
        }

        float atlasU = sprite.getU0() + ru * (sprite.getU1() - sprite.getU0());
        float atlasV = sprite.getV0() + rv * (sprite.getV1() - sprite.getV0());

        emitter.uv(vertex, atlasU, atlasV);
    }


}
