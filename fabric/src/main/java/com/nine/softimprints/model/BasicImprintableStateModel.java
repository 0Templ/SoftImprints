package com.nine.softimprints.model;

import com.nine.softimprints.client.core.cache.CacheAccess;
import com.nine.softimprints.client.core.cache.LevelData;
import com.nine.softimprints.client.core.map.IImprintMap;
import com.nine.softimprints.client.model.BlockRenderData;
import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.ImprintResolveContext;
import com.nine.softimprints.client.profile.ProfileResolverEntry;
import com.nine.softimprints.client.profile.ResolvedImprintProfile;
import com.nine.softimprints.model.render.*;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class BasicImprintableStateModel extends ImprintableStateModel {

    private final List<ProfileResolverEntry> modelResolvers;

    public BasicImprintableStateModel(BlockStateModel wrapped) {
        super(wrapped);
        this.modelResolvers = List.of();
    }

    private BasicImprintableStateModel(
            BlockStateModel wrapped,
            List<ProfileResolverEntry> modelResolvers
    ) {
        super(wrapped);
        this.modelResolvers = sortResolvers(modelResolvers);
    }

    public BlockStateModel wrappedModel() {
        return this.wrapped;
    }

    public BasicImprintableStateModel withModelResolver(ProfileResolverEntry resolver) {
        List<ProfileResolverEntry> next = new ArrayList<>(this.modelResolvers);
        next.add(resolver);
        return new BasicImprintableStateModel(this.wrapped, next);
    }

    @Override
    public void emitQuads(
            QuadEmitter emitter,
            BlockAndTintGetter blockView,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            Predicate<Direction> cullTest
    ) {
        LevelData levelData = CacheAccess.current();
        if (levelData == null) {
            wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
            return;
        }

        IImprintMap map = levelData.getImprintCache().getImprintMap(pos.asLong());
        ResolvedImprintProfile resolved = resolveProfile(blockView, pos, state);

        if (map == null || map.isEmpty() || resolved == null) {
            wrapped.emitQuads(emitter, blockView, pos, state, random, cullTest);
            return;
        }

        BlockRenderData blockRenderData = levelData.getBlockRenderCache().get(pos.asLong(), wrapped, state);

        ImprintRenderContext context = new ImprintRenderContext(
                wrapped,
                emitter,
                blockView,
                pos,
                state,
                random,
                cullTest,
                resolved.profile(),
                resolved.surface(),
                map,
                blockRenderData
        );

        Float forcedOverlayY = resolved.renderOverrides().forcedOverlayY();
        if (forcedOverlayY != null) {
            ForcedOverlaySurfaceRenderer.emit(context, forcedOverlayY);
            return;
        }

        getRenderer(resolved.surface().mode()).emit(context);
    }

    private static final ImprintSurfaceRenderer OVERLAY_SURFACE_RENDERER = new OverlaySurfaceRenderer();

    private static final ImprintSurfaceRenderer TOP_SURFACE_RENDERER = new TopSurfaceRenderer();

    private static ImprintSurfaceRenderer getRenderer(SurfaceMode mode) {
        return switch (mode) {
            case OVERLAY -> OVERLAY_SURFACE_RENDERER;
            case TOP -> TOP_SURFACE_RENDERER;
        };
    }

    private ResolvedImprintProfile resolveProfile(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        if (!this.modelResolvers.isEmpty()) {
            ImprintResolveContext context = new ImprintResolveContext(level, pos, state);
            for (ProfileResolverEntry entry : this.modelResolvers) {
                ResolvedImprintProfile resolved = ImprintProfiles.resolve(entry.resolver().resolve(context));
                if (resolved != null) {
                    return resolved;
                }
            }
        }
        return ImprintProfiles.resolve(level, pos, state);
    }

    private static List<ProfileResolverEntry> sortResolvers(List<ProfileResolverEntry> resolvers) {
        return resolvers.stream()
                .sorted(Comparator.comparingInt(ProfileResolverEntry::priority).reversed())
                .toList();
    }

}
