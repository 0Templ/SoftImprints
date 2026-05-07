package com.nine.softimprints.model.render;

import com.mojang.blaze3d.platform.Transparency;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nine.softimprints.client.core.map.ImprintStrip;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;

public final class EmitHelper {

    private EmitHelper() {
    }

    public static BakedQuad buildStripQuad(
            TextureAtlasSprite sprite,
            ImprintStrip strip,
            int mapSize,
            float topY,
            int rotation
    ) {
        float x0 = strip.x0() / (float) mapSize;
        float x1 = strip.x1() / (float) mapSize;
        float z0 = strip.y0() / (float) mapSize;
        float z1 = (strip.y0() + 1) / (float) mapSize;

        float spriteU0 = strip.x0() / (float) mapSize;
        float spriteU1 = strip.x1() / (float) mapSize;
        float spriteV0 = strip.y0() / (float) mapSize;
        float spriteV1 = (strip.y0() + 1) / (float) mapSize;

        QuadBakingVertexConsumer builder = new QuadBakingVertexConsumer();
        builder.setDirection(Direction.UP);
        builder.setSprite(new Material.Baked(sprite, false), Transparency.TRANSPARENT);
        builder.setTintIndex(-1);
        builder.setShade(true);
        builder.setAmbientOcclusion(true);

        emitCorner(builder, sprite, x0, topY, z0, spriteU0, spriteV0, rotation);
        emitCorner(builder, sprite, x0, topY, z1, spriteU0, spriteV1, rotation);
        emitCorner(builder, sprite, x1, topY, z1, spriteU1, spriteV1, rotation);
        emitCorner(builder, sprite, x1, topY, z0, spriteU1, spriteV0, rotation);

        return builder.bakeQuad();
    }

    private static void emitCorner(
            VertexConsumer consumer,
            TextureAtlasSprite sprite,
            float x, float y, float z,
            float u, float v,
            int rotation
    ) {
        float ru;
        float rv;
        switch (rotation & 3) {
            case 1 -> { ru = 1.0F - v; rv = u; }
            case 2 -> { ru = 1.0F - u; rv = 1.0F - v; }
            case 3 -> { ru = v; rv = 1.0F - u; }
            default -> { ru = u; rv = v; }
        }
        float atlasU = sprite.getU0() + ru * (sprite.getU1() - sprite.getU0());
        float atlasV = sprite.getV0() + rv * (sprite.getV1() - sprite.getV0());

        consumer.addVertex(x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(atlasU, atlasV)
                .setNormal(0.0f, 1.0f, 0.0f);
    }
}
