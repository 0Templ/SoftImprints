package com.nine.softimprints.client.profile.options.surface;

import com.nine.softimprints.client.model.SurfaceMode;

public record ImprintSurfaceSettings(
        SurfaceMode mode,
        ZeroLayerSource zeroLayerSource
) {

    public static final ImprintSurfaceSettings DEFAULT = new ImprintSurfaceSettings(
            SurfaceMode.TOP,
            ZeroLayerSource.SURFACE
    );

    public ImprintSurfaceSettings {
        if (mode == null) {
            mode = DEFAULT.mode;
        }
        if (zeroLayerSource == null) {
            zeroLayerSource = DEFAULT.zeroLayerSource;
        }
    }

    public boolean useOriginalZeroLayer() {
        return zeroLayerSource == ZeroLayerSource.SURFACE;
    }

    public ImprintSurfaceSettings withMode(SurfaceMode mode) {
        return new ImprintSurfaceSettings(mode, zeroLayerSource);
    }

    public ImprintSurfaceSettings withZeroLayerSource(ZeroLayerSource zeroLayerSource) {
        return new ImprintSurfaceSettings(mode, zeroLayerSource);
    }

    public ImprintSurfaceSettings toggleZeroLayerSource() {
        return withZeroLayerSource(useOriginalZeroLayer()
                ? ZeroLayerSource.PROFILE
                : ZeroLayerSource.SURFACE);
    }
}
