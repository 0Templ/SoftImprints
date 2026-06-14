package com.nine.softimprints.api.plugin;

import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.profile.resolver.ProfileResolverEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.*;

public class ImprintPlugins {

    private static final Map<Block, List<ProfileResolverEntry>> profileResolvers = new HashMap<>();

    private static final List<ImprintPluginInfo> pluginsMeta = new ArrayList<>();

    private static final ImprintRegistrar REGISTRAR = new RegistrarImpl();

    public static ImprintRegistrar registrar() {
        return REGISTRAR;
    }

    public static Map<Block, List<ProfileResolverEntry>> profileResolvers() {
        Map<Block, List<ProfileResolverEntry>> snap = new HashMap<>();
        for (var entry : profileResolvers.entrySet()) {
            snap.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Map.copyOf(snap);
    }

    // Is it greedy?
    public static boolean isPluginEnabled(Identifier id) {
        return !SIConfig.Plugins.DISABLED_PLUGINS.get().contains(id.toString());
    }

    public static List<ImprintPluginInfo> pluginsMeta() {
        return pluginsMeta;
    }

    public static void addMeta(ImprintPluginInfo info) {
        pluginsMeta.add(info);
    }

    private static final class RegistrarImpl implements ImprintRegistrar {

        @Override
        public void registerResolver(
                Block block,
                ProfileResolverEntry resolver
        ) {
            Objects.requireNonNull(block, "block");
            Objects.requireNonNull(resolver, "resolver");
            profileResolvers
                    .computeIfAbsent(block, key -> new ArrayList<>())
                    .add(resolver);
        }
    }

}
