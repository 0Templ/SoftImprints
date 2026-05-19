package com.nine.softimprints.client.profile.resolver;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public record ProfileCandidate(
        @Nullable Identifier profileId,
        int priority,
        Source source,
        @Nullable Identifier pluginId
) {

    public enum Source {
        STATIC_PROFILE,
        PLUGIN_RESOLVER
    }
}
