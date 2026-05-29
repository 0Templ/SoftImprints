package com.nine.softimprints.profile.resolver;

import com.nine.softimprints.model.SurfaceMode;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;
import com.nine.softimprints.profile.options.surface.ZeroLayerSource;

import javax.annotation.Nullable;


// For render only
public record ImprintRenderOverrides(
        @Nullable SurfaceMode surfaceMode,
        @Nullable ZeroLayerSource zeroLayerSource,
        @Nullable Float forcedOverlayY
) {

    public static final ImprintRenderOverrides EMPTY = new ImprintRenderOverrides(null, null, null);

    public boolean empty() {
        return surfaceMode == null && zeroLayerSource == null && forcedOverlayY == null;
    }

    public SurfaceSettings applyTo(SurfaceSettings base) {
        if (empty()) {
            return base;
        }
        return new SurfaceSettings(
                surfaceMode != null ? surfaceMode : base.mode(),
                zeroLayerSource != null ? zeroLayerSource : base.zeroLayerSource()
        );
    }
}
