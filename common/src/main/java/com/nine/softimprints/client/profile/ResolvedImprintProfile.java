package com.nine.softimprints.client.profile;

import com.nine.softimprints.client.profile.options.surface.ImprintSurfaceSettings;

import java.util.Objects;

public record ResolvedImprintProfile(
        ImprintProfile profile,
        ImprintSurfaceSettings surface,
        ImprintRenderOverrides renderOverrides
) {

    public ResolvedImprintProfile {
        if (renderOverrides == null) {
            renderOverrides = ImprintRenderOverrides.EMPTY;
        }
    }

    public static ResolvedImprintProfile fromProfile(ImprintProfile profile) {
        return new ResolvedImprintProfile(
                profile,
                profile.surface(),
                ImprintRenderOverrides.EMPTY
        );
    }
}
