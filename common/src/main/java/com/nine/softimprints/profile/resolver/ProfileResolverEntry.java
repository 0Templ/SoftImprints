package com.nine.softimprints.profile.resolver;

import net.minecraft.resources.Identifier;

public record ProfileResolverEntry(
        Identifier pluginId,
        int priority,
        ProfileResolver resolver
) {

}
