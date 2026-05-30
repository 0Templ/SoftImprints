package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.IImprintMap;
import com.nine.softimprints.model.BlockRenderData;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public record ImprintRenderContext(
        BlockStateModel wrapped,
        List<BlockStateModelPart> parts,
        BlockAndTintGetter level,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        ImprintProfile profile,
        SurfaceSettings surface,
        IImprintMap map,
        BlockRenderData blockRenderData
) {}
