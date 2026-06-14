package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.IImprintMap;
import com.nine.softimprints.model.BlockRenderData;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;

public record ImprintRenderContext(
        BlockStateModel wrapped,
        QuadEmitter emitter,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        Predicate<Direction> cullTest,
        ImprintProfile profile,
        SurfaceSettings surface,
        IImprintMap map,
        BlockRenderData blockRenderData
) {
}
