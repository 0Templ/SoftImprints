package com.nine.softimprints.client.profile;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.client.api.plugin.ImprintPlugins;
import com.nine.softimprints.client.profile.io.json.JsonProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public class ImprintProfiles {

    private static volatile Map<Identifier, ImprintProfile> byId = Map.of();
    private static volatile Map<Block, Identifier> blockToId = Map.of();
    private static volatile Map<Identifier, JsonProfile> builtinJsonById = Map.of();
    private static volatile Map<Identifier, ImprintProfile> builtinById = Map.of();

    private static volatile Map<Block, List<ProfileResolverEntry>> blockToResolver = Map.of();

    @Nullable
    public static ResolvedImprintProfile resolve(BlockGetter level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        ImprintResolveContext context = null;

        for (ProfileResolverEntry entry : blockToResolver.getOrDefault(block, List.of())) {
            if (!ImprintPlugins.isPluginEnabled(entry.pluginId())) continue;

            if (context == null) {
                context = new ImprintResolveContext(level, pos, state);
            }

            ImprintResolveResult result = entry.resolver().resolve(context);
            if (result == null) {
                continue;
            }

            ResolvedImprintProfile resolved = resolve(result);
            if (resolved != null) {
                return resolved;
            }
        }

        return resolve(blockToId.get(block));
    }

    @Nullable
    public static ResolvedImprintProfile resolve(@Nullable ImprintResolveResult result) {
        if (result == null) {
            return null;
        }
        ImprintProfile profile = byId.get(result.profileId());
        if (profile == null) {
            return null;
        }
        return new ResolvedImprintProfile(
                profile,
                result.renderOverrides().applyTo(profile.surface()),
                result.renderOverrides()
        );
    }

    @Nullable
    public static ImprintProfile getProfile(BlockGetter level, BlockPos pos, BlockState state) {
        ResolvedImprintProfile resolved = resolve(level, pos, state);
        return resolved == null ? null : resolved.profile();
    }


    @Nullable
    public static ImprintProfile getProfile(@Nullable Identifier id) {
        if (id == null) {
            return null;
        }
        return byId.get(id);
    }

    @Nullable
    private static ResolvedImprintProfile resolve(@Nullable Identifier id) {
        ImprintProfile profile = getProfile(id);
        return profile == null ? null : ResolvedImprintProfile.fromProfile(profile);
    }

    public static boolean supportsBlock(Block block) {
        return blockToId.containsKey(block) || blockToResolver.containsKey(block);
    }

    public static Set<Block> supportedBlocks() {
        Set<Block> ret = new HashSet<>();
        ret.addAll(blockToId.keySet());
        ret.addAll(blockToResolver.keySet());
        return ret;
    }

    public static JsonProfile getBuiltInJson(Identifier id) {
        return builtinJsonById.get(id);
    }

    public static Map<Identifier, ImprintProfile> snapshot() {
        return byId;
    }

    public static void replaceMain(
            Map<Identifier, ImprintProfile> next
    ) {
        Map<Block, Identifier> reverse = buildReverse(next);
        Map<Block, List<ProfileResolverEntry>> resolvers = buildResolvers(ImprintPlugins.profileResolvers());

        byId = Map.copyOf(next);
        blockToId = Map.copyOf(reverse);
        blockToResolver = resolvers;
    }

    private static Map<Block, List<ProfileResolverEntry>> buildResolvers(Map<Block, List<ProfileResolverEntry>> resolvers) {
        Map<Block, List<ProfileResolverEntry>> out = new HashMap<>();
        for (var entry : resolvers.entrySet()) {
            List<ProfileResolverEntry> sorted = entry.getValue().stream()
                    .sorted(Comparator.comparingInt(ProfileResolverEntry::priority).reversed())
                    .toList();
            if (!sorted.isEmpty()) {
                out.put(entry.getKey(), sorted);
            }
        }
        return Map.copyOf(out);
    }

    public static void replaceBuiltin(
            Map<Identifier, JsonProfile> raw,
            Map<Identifier, ImprintProfile> ready
    ) {
        builtinJsonById = Map.copyOf(raw);
        builtinById = Map.copyOf(ready);
    }

    public static Stream<ImprintProfile> profiles(){
        return byId.values().stream();
    }

    public static Stream<ImprintProfile> builtInProfiles(){
        return builtinById.values().stream();
    }

    private static Map<Block, Identifier> buildReverse(Map<Identifier, ImprintProfile> src) {
        Map<Block, Identifier> out = new HashMap<>();
        for (var e : src.entrySet()) {
            for (var surfaceBlock : e.getValue().supportedBlocks()) {
                if (!surfaceBlock.resolved()) continue;
                var block = surfaceBlock.block();
                var prev = out.putIfAbsent(block, e.getKey());
                if (prev != null) {
                    SICommon.LOGGER.warn("Block {} claimed by both {} and {}, keeping {}",
                            block, prev, e.getKey(), prev);
                }
            }
        }
        return out;
    }

}
