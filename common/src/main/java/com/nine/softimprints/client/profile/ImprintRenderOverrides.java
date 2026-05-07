package com.nine.softimprints.client.profile;

import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.options.surface.ImprintSurfaceSettings;
import com.nine.softimprints.client.profile.options.surface.ZeroLayerSource;

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

    public ImprintSurfaceSettings applyTo(ImprintSurfaceSettings base) {
        if (empty()) {
            return base;
        }
        return new ImprintSurfaceSettings(
                surfaceMode != null ? surfaceMode : base.mode(),
                zeroLayerSource != null ? zeroLayerSource : base.zeroLayerSource()
        );
    }
}
