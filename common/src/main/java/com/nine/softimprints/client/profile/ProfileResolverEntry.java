package com.nine.softimprints.client.profile;

import net.minecraft.resources.Identifier;

public record ProfileResolverEntry(
        Identifier pluginId,
        int priority,
        ProfileResolver resolver
) {

}
