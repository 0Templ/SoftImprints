package com.nine.softimprints.client.profile.io.json;

import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.options.surface.ZeroLayerSource;

import java.util.Objects;

public record JsonSurfaceSettings(
        SurfaceMode mode,
        ZeroLayerSource zeroLayerSource
) {

    public JsonSurfaceSettings merge(JsonSurfaceSettings with) {
        if (with == null) return this;

        return new JsonSurfaceSettings(
                with.mode != null ? with.mode : this.mode,
                with.zeroLayerSource != null ? with.zeroLayerSource : this.zeroLayerSource
        );
    }

    public JsonSurfaceSettings nullifyAgainst(JsonSurfaceSettings with) {
        if (with == null) return this;

        return new JsonSurfaceSettings(
                Objects.equals(with.mode, this.mode) ? null : this.mode,
                Objects.equals(with.zeroLayerSource, this.zeroLayerSource) ? null : this.zeroLayerSource
        );
    }
}
