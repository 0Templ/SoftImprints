package com.nine.softimprints.profile.resolver;

import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;

public record ResolvedImprintProfile(
        ImprintProfile profile,
        SurfaceSettings surface,
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
