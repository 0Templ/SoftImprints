package com.nine.softimprints.profile;

import com.nine.softimprints.api.plugin.ImprintPlugins;
import com.nine.softimprints.profile.catalog.entry.ImprintProfileEntry;
import com.nine.softimprints.profile.io.json.JsonProfile;
import com.nine.softimprints.profile.resolver.ImprintResolveResult;
import com.nine.softimprints.profile.resolver.ProfileCandidate;
import com.nine.softimprints.profile.resolver.ProfileResolveIndex;
import com.nine.softimprints.profile.resolver.ResolvedImprintProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ImprintProfiles {

    private static volatile ProfileResolveIndex resolveIndex = ProfileResolveIndex.empty();

    private static volatile Map<Identifier, ImprintProfile> byId = Map.of();

    private static volatile Map<Identifier, JsonProfile> builtinJsonById = Map.of();
    private static volatile Map<Identifier, ImprintProfile> builtinById = Map.of();
    private static volatile Map<Identifier, ImprintProfileEntry> entriesById = Map.of();

    @Nullable
    public static ResolvedImprintProfile resolve(
            BlockGetter level,
            BlockPos pos,
            BlockState state
    ) {
        return resolve(resolveIndex.resolve(level, pos, state));
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
    public static ImprintProfile getProfile(
            BlockGetter level,
            BlockPos pos,
            BlockState state
    ) {
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
        return resolveIndex.supportsBlock(block);
    }

    public static List<ProfileCandidate> profileCandidates(Block block) {
        return resolveIndex.profileCandidates(block);
    }

    public static Set<Block> supportedBlocks() {
        return resolveIndex.supportedBlocks();
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
        byId = Map.copyOf(next);
        resolveIndex = ProfileResolveIndex.build(byId, ImprintPlugins.profileResolvers());
    }

    public static void replaceBuiltin(
            Map<Identifier, JsonProfile> raw,
            Map<Identifier, ImprintProfile> ready
    ) {
        builtinJsonById = Map.copyOf(raw);
        builtinById = Map.copyOf(ready);
    }

    public static void replaceEntries(
            Map<Identifier, ImprintProfileEntry> ready
    ) {
        entriesById = Map.copyOf(ready);
    }

    public static Map<Identifier, ImprintProfile> profiles() {
        return byId;
    }

    public static Map<Identifier, ImprintProfile> builtInProfiles() {
        return builtinById;
    }

    public static Map<Identifier, ImprintProfileEntry> entriesById() {
        return entriesById;
    }

}
