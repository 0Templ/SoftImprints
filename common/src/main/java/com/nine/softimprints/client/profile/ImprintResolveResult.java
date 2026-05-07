package com.nine.softimprints.client.profile;

import net.minecraft.resources.Identifier;

import java.util.Objects;

public record ImprintResolveResult(
        Identifier profileId,
        ImprintRenderOverrides renderOverrides
) {

    public ImprintResolveResult {
        Objects.requireNonNull(profileId, "profileId");
        if (renderOverrides == null) {
            renderOverrides = ImprintRenderOverrides.EMPTY;
        }
    }

    public static ImprintResolveResult profile(Identifier profileId) {
        return new ImprintResolveResult(profileId, ImprintRenderOverrides.EMPTY);
    }

    public static ImprintResolveResult profile(
            Identifier profileId,
            ImprintRenderOverrides renderOverrides
    ) {
        return new ImprintResolveResult(profileId, renderOverrides);
    }
}
