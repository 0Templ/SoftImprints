package com.nine.softimprints.profile.resolver;

import com.nine.softimprints.api.plugin.ImprintPlugins;
import com.nine.softimprints.profile.ImprintProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public final class ProfileResolveIndex {

    private static final ProfileResolveIndex EMPTY = new ProfileResolveIndex(Map.of(), Map.of(), Set.of());

    private final Map<Block, List<Route>> routesByBlock;
    private final Map<Block, List<ProfileCandidate>> candidatesByBlock;
    private final Set<Identifier> knownProfiles;

    private ProfileResolveIndex(
            Map<Block, List<Route>> routesByBlock,
            Map<Block, List<ProfileCandidate>> candidatesByBlock,
            Set<Identifier> knownProfiles
    ) {
        this.routesByBlock = routesByBlock;
        this.candidatesByBlock = candidatesByBlock;
        this.knownProfiles = knownProfiles;
    }

    public static ProfileResolveIndex empty() {
        return EMPTY;
    }

    public static ProfileResolveIndex build(
            Map<Identifier, ImprintProfile> profiles,
            Map<Block, List<ProfileResolverEntry>> pluginResolvers
    ) {
        Map<Block, List<Route>> mutable = new HashMap<>();

        for (ImprintProfile profile : profiles.values()) {
            for (var surfaceBlock : profile.supportedBlocks()) {
                if (!surfaceBlock.resolved()) {
                    continue;
                }
                mutable.computeIfAbsent(surfaceBlock.block(), key -> new ArrayList<>())
                        .add(new StaticRoute(profile.id(), profile.priority()));
            }
        }

        for (var entry : pluginResolvers.entrySet()) {
            for (ProfileResolverEntry resolver : entry.getValue()) {
                mutable.computeIfAbsent(entry.getKey(), key -> new ArrayList<>())
                        .add(new PluginRoute(resolver));
            }
        }

        Map<Block, List<Route>> routesByBlock = new HashMap<>();
        Map<Block, List<ProfileCandidate>> candidatesByBlock = new HashMap<>();
        for (var entry : mutable.entrySet()) {
            List<Route> sorted = entry.getValue().stream()
                    .sorted(Route.ORDER)
                    .toList();
            if (!sorted.isEmpty()) {
                routesByBlock.put(entry.getKey(), sorted);
                candidatesByBlock.put(entry.getKey(), sorted.stream()
                        .map(Route::candidate)
                        .toList());
            }
        }

        return new ProfileResolveIndex(
                Map.copyOf(routesByBlock),
                Map.copyOf(candidatesByBlock),
                Set.copyOf(profiles.keySet())
        );
    }

    @Nullable
    public ImprintResolveResult resolve(BlockGetter level, BlockPos pos, BlockState state) {
        List<Route> routes = routesByBlock.get(state.getBlock());
        if (routes == null || routes.isEmpty()) {
            return null;
        }

        ImprintResolveContext context = null;
        for (Route route : routes) {
            if (!route.available()) {
                continue;
            }
            if (context == null) {
                context = new ImprintResolveContext(level, pos, state);
            }

            ImprintResolveResult result = route.resolve(context);
            if (result != null && knownProfiles.contains(result.profileId())) {
                return result;
            }
        }
        return null;
    }

    public boolean supportsBlock(Block block) {
        return routesByBlock.containsKey(block);
    }

    public List<ProfileCandidate> profileCandidates(Block block) {
        return candidatesByBlock.getOrDefault(block, List.of());
    }

    public Set<Block> supportedBlocks() {
        return routesByBlock.keySet();
    }

    private interface Route {

        Comparator<Route> ORDER = Comparator
                .comparingInt(Route::priority)
                .reversed()
                .thenComparing(Route::sourceKey);

        int priority();

        String sourceKey();

        ProfileCandidate candidate();

        default boolean available() {
            return true;
        }

        @Nullable
        ImprintResolveResult resolve(ImprintResolveContext context);
    }

    private record StaticRoute(
            Identifier profileId,
            int priority
    ) implements Route {

        @Override
        public String sourceKey() {
            return profileId.toString();
        }

        @Override
        public ProfileCandidate candidate() {
            return new ProfileCandidate(
                    profileId,
                    priority,
                    ProfileCandidate.Source.STATIC_PROFILE,
                    null
            );
        }

        @Override
        public ImprintResolveResult resolve(ImprintResolveContext context) {
            return ImprintResolveResult.profile(profileId);
        }
    }

    private record PluginRoute(ProfileResolverEntry entry) implements Route {

        @Override
        public int priority() {
            return entry.priority();
        }

        @Override
        public String sourceKey() {
            return entry.pluginId() + ":" + entry.priority();
        }

        @Override
        public ProfileCandidate candidate() {
            return new ProfileCandidate(
                    null,
                    entry.priority(),
                    ProfileCandidate.Source.PLUGIN_RESOLVER,
                    entry.pluginId()
            );
        }

        @Override
        public boolean available() {
            return ImprintPlugins.isPluginEnabled(entry.pluginId());
        }

        @Override
        public ImprintResolveResult resolve(ImprintResolveContext context) {
            return entry.resolver().resolve(context);
        }
    }
}
