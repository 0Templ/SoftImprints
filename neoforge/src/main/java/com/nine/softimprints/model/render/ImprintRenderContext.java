package com.nine.softimprints.model.render;

import com.nine.softimprints.client.core.map.IImprintMap;
import com.nine.softimprints.client.model.BlockRenderData;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.profile.options.surface.SurfaceSettings;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Per-render call context handed to a NeoForge {@link ImprintSurfaceRenderer}.
 *
 * <p>Mirrors the fabric-side context, but with a parts list (NeoForge's quad-list API)
 * instead of a Fabric {@code QuadEmitter}.
 */
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
