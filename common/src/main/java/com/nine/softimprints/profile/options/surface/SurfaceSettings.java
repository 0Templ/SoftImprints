package com.nine.softimprints.profile.options.surface;

import com.nine.softimprints.model.SurfaceMode;

public record SurfaceSettings(
        SurfaceMode mode,
        ZeroLayerSource zeroLayerSource
) {

    public static final SurfaceSettings DEFAULT = new SurfaceSettings(
            SurfaceMode.REPAINT,
            ZeroLayerSource.SURFACE
    );

    public SurfaceSettings {
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

    public SurfaceSettings withMode(SurfaceMode mode) {
        return new SurfaceSettings(mode, zeroLayerSource);
    }

    public SurfaceSettings withZeroLayerSource(ZeroLayerSource zeroLayerSource) {
        return new SurfaceSettings(mode, zeroLayerSource);
    }

    public SurfaceSettings toggleZeroLayerSource() {
        return withZeroLayerSource(useOriginalZeroLayer()
                ? ZeroLayerSource.PROFILE
                : ZeroLayerSource.SURFACE);
    }
}
