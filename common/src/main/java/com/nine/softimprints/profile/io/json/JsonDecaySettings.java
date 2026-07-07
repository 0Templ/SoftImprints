package com.nine.softimprints.profile.io.json;

import com.google.gson.annotations.SerializedName;

public record JsonDecaySettings(
        Boolean enabled,
        @SerializedName("grace_seconds")
        Integer graceSeconds,
        @SerializedName("ramp_seconds")
        Integer rampSeconds,
        Double chance,
        @SerializedName("depth_bias")
        Double depthBias
) {

    public JsonDecaySettings merge(JsonDecaySettings with) {
        return new JsonDecaySettings(
                with.enabled() != null ? with.enabled() : this.enabled,
                with.graceSeconds() != null ? with.graceSeconds() : this.graceSeconds,
                with.rampSeconds() != null ? with.rampSeconds() : this.rampSeconds,
                with.chance() != null ? with.chance() : this.chance,
                with.depthBias() != null ? with.depthBias() : this.depthBias
        );
    }

}
