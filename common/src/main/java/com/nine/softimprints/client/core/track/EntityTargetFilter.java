package com.nine.softimprints.client.core.track;

import com.nine.softimprints.client.config.EntityTargetFilterMode;
import com.nine.softimprints.client.config.SIConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class EntityTargetFilter {

    private static Snapshot cached = new Snapshot(EntityTargetFilterMode.BLACKLIST, Set.of(), Set.of());
    private static EntityTargetFilterMode cachedMode = EntityTargetFilterMode.BLACKLIST;
    private static List<String> cachedWhitelistIds = List.of();
    private static List<String> cachedBlacklistIds = List.of();

    private EntityTargetFilter() {
    }

    public static Snapshot current() {
        EntityTargetFilterMode mode = SIConfig.Targets.IMPRINT_TARGET_FILTER_MODE.get();
        List<String> whitelistIds = copyIds(SIConfig.Targets.IMPRINT_TARGET_WHITELIST.get());
        List<String> blacklistIds = copyIds(SIConfig.Targets.IMPRINT_TARGET_BLACKLIST.get());

        synchronized (EntityTargetFilter.class) {
            if (mode == cachedMode
                    && whitelistIds.equals(cachedWhitelistIds)
                    && blacklistIds.equals(cachedBlacklistIds)) {
                return cached;
            }

            cachedMode = mode;
            cachedWhitelistIds = whitelistIds;
            cachedBlacklistIds = blacklistIds;
            cached = new Snapshot(
                    mode,
                    toIdentifierSet(whitelistIds),
                    toIdentifierSet(blacklistIds)
            );
            return cached;
        }
    }

    private static List<String> copyIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<String> result = new ArrayList<>(ids.size());
        for (String raw : ids) {
            if (raw == null) continue;
            String id = raw.trim();
            if (!id.isEmpty()) {
                result.add(id);
            }
        }
        return List.copyOf(result);
    }

    private static Set<Identifier> toIdentifierSet(List<String> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        Set<Identifier> result = new LinkedHashSet<>();
        for (String raw : ids) {
            Identifier id = Identifier.tryParse(raw);
            if (id != null) {
                result.add(id);
            }
        }
        return Set.copyOf(result);
    }

    public record Snapshot(
            EntityTargetFilterMode mode,
            Set<Identifier> whitelist,
            Set<Identifier> blacklist
    ) {
        public boolean allows(Entity entity) {
            if (entity == null) {
                return false;
            }

            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            return switch (mode) {
                case WHITELIST -> whitelist.contains(id);
                case BLACKLIST -> !blacklist.contains(id);
            };
        }
    }
}
