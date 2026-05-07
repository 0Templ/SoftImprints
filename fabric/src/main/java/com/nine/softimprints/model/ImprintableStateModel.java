package com.nine.softimprints.model;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.model.FabricBlockStateModel;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ImprintableStateModel implements BlockStateModel, FabricBlockStateModel {

    protected BlockStateModel wrapped;

    protected ImprintableStateModel(BlockStateModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public void collectParts(
            RandomSource random,
            List<BlockStateModelPart> output
    ) {
        wrapped.collectParts(random, output);
    }

    @Override
    public void emitQuads(QuadEmitter emitter, BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random, Predicate<@Nullable Direction> cullTest) {
        this.wrapped.emitQuads(emitter, level, pos, state, random, cullTest);
    }


    @Override
    public @Nullable Object createGeometryKey(BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random) {
        return this.wrapped.createGeometryKey(level, pos, state, random);
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.wrapped.particleMaterial();
    }

    @Override
    @BakedQuad.MaterialFlags
    public int materialFlags() {
        return this.wrapped.materialFlags();
    }
}
