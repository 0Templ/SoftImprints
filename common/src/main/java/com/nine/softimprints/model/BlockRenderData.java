package com.nine.softimprints.model;

import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3fc;

import java.util.ArrayList;
import java.util.List;

public record BlockRenderData(int rotation, TextureAtlasSprite topSprite) {

    public static BlockRenderData compute(
            BlockPos pos,
            BlockStateModel wrapped,
            BlockState state
    ) {
        long seed = state.getSeed(pos);
        RandomSource random = RandomSource.create(seed);

        List<BlockStateModelPart> parts = new ArrayList<>();
        wrapped.collectParts(random, parts);

        TextureAtlasSprite sprite = computeTopSprite(parts, wrapped);

        int rotation = computeTopRotation(parts, sprite);

        return new BlockRenderData(rotation, sprite);
    }

    public static TextureAtlasSprite computeTopSprite(
            List<BlockStateModelPart> parts,
            BlockStateModel wrapped
    ) {
        if (parts.isEmpty()) return wrapped.particleMaterial().sprite();
        List<BakedQuad> quads = parts.getFirst().getQuads(Direction.UP);
        if (quads.isEmpty()) return wrapped.particleMaterial().sprite();
        return quads.getFirst().materialInfo().sprite();
    }

    public static int computeTopRotation(
            List<BlockStateModelPart> parts,
            TextureAtlasSprite sprite
    ) {

        if (parts.isEmpty()) return 0;

        List<BakedQuad> upQuads = parts.getFirst().getQuads(Direction.UP);
        if (upQuads.isEmpty()) return 0;

        BakedQuad q = upQuads.getFirst();

        Vector3fc[] positions = {q.position0(), q.position1(), q.position2(), q.position3()};
        long[] uvs = {q.packedUV0(), q.packedUV1(), q.packedUV2(), q.packedUV3()};

        int targetVertex = 0;
        float minSum = Float.POSITIVE_INFINITY;
        for (int i = 0; i < 4; i++) {
            float sum = positions[i].x() + positions[i].z();
            if (sum < minSum) {
                minSum = sum;
                targetVertex = i;
            }
        }

        float u = unpackU(uvs[targetVertex]);
        float v = unpackV(uvs[targetVertex]);

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();
        final float EPS = 1e-4f;

        if (Math.abs(u - u0) < EPS && Math.abs(v - v0) < EPS) return 0;
        if (Math.abs(u - u1) < EPS && Math.abs(v - v0) < EPS) return 1;
        if (Math.abs(u - u1) < EPS && Math.abs(v - v1) < EPS) return 2;
        if (Math.abs(u - u0) < EPS && Math.abs(v - v1) < EPS) return 3;

        return 0;
    }

    private static float unpackU(long packedUV) {
        return Float.intBitsToFloat((int) ((packedUV >>> 32) & 0xFFFFFFFFL));
    }

    private static float unpackV(long packedUV) {
        return Float.intBitsToFloat((int) (packedUV & 0xFFFFFFFFL));
    }

}
