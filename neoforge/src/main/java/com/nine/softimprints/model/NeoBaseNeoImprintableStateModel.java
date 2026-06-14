package com.nine.softimprints.model;

import com.nine.softimprints.core.cache.CacheAccess;
import com.nine.softimprints.core.cache.LevelData;
import com.nine.softimprints.core.map.IImprintMap;
import com.nine.softimprints.model.render.*;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.resolver.ImprintResolveContext;
import com.nine.softimprints.profile.resolver.ProfileResolverEntry;
import com.nine.softimprints.profile.resolver.ResolvedImprintProfile;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class NeoBaseNeoImprintableStateModel extends NeoImprintableStateModel {

    private static final ImprintSurfaceRenderer OVERLAY_SURFACE_RENDERER = new OverlaySurfaceRenderer();
    private static final ImprintSurfaceRenderer TOP_SURFACE_RENDERER = new TopSurfaceRenderer();

    private final List<ProfileResolverEntry> modelResolvers;

    public NeoBaseNeoImprintableStateModel(BlockStateModel wrapped) {
        this(wrapped, List.of());
    }

    private NeoBaseNeoImprintableStateModel(
            BlockStateModel wrapped,
            List<ProfileResolverEntry> modelResolvers
    ) {
        super(wrapped);
        this.modelResolvers = sortResolvers(modelResolvers);
    }

    private static ImprintSurfaceRenderer getRenderer(SurfaceMode mode) {
        return switch (mode) {
            case OVERLAY -> OVERLAY_SURFACE_RENDERER;
            case REPAINT -> TOP_SURFACE_RENDERER;
        };
    }

    private static List<ProfileResolverEntry> sortResolvers(List<ProfileResolverEntry> resolvers) {
        return resolvers.stream()
                .sorted(Comparator.comparingInt(ProfileResolverEntry::priority).reversed())
                .toList();
    }

    public BlockStateModel wrappedModel() {
        return this.delegate;
    }

    public NeoBaseNeoImprintableStateModel withModelResolver(ProfileResolverEntry resolver) {
        List<ProfileResolverEntry> next = new ArrayList<>(this.modelResolvers);
        next.add(resolver);
        return new NeoBaseNeoImprintableStateModel(this.delegate, next);
    }

    @Override
    public void collectParts(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> parts
    ) {
        super.collectParts(level, pos, state, random, parts);

        LevelData levelData = CacheAccess.current();
        if (levelData == null) {
            return;
        }

        IImprintMap map = levelData.getImprintCache().getImprintMap(pos.asLong());
        if (map == null || map.isEmpty()) {
            return;
        }

        BlockRenderData blockRenderData = levelData.getBlockRenderCache().get(pos.asLong(), this.delegate, state);
        ResolvedImprintProfile resolved = resolveProfile(level, pos, state);
        if (resolved == null) {
            return;
        }

        ImprintRenderContext context = buildContext(level, pos, state, random, parts, resolved, map, blockRenderData);
        Float forcedOverlayY = resolved.renderOverrides().forcedOverlayY();
        if (forcedOverlayY != null) {
            ForcedOverlaySurfaceRenderer.emit(context, forcedOverlayY);
            return;
        }

        getRenderer(resolved.surface().mode()).emit(context);
    }

    private ImprintRenderContext buildContext(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            RandomSource random,
            List<BlockStateModelPart> parts,
            ResolvedImprintProfile resolved,
            IImprintMap map,
            BlockRenderData blockRenderData
    ) {
        return new ImprintRenderContext(
                this.delegate,
                parts,
                level,
                pos,
                state,
                random,
                resolved.profile(),
                resolved.surface(),
                map,
                blockRenderData
        );
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
}
