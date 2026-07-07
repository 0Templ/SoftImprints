package com.nine.softimprints.profile.options.decay;

import javax.annotation.Nullable;

public record ProfileDecaySettings(
        boolean enabled,
        @Nullable Integer graceSeconds,
        @Nullable Integer rampSeconds,
        @Nullable Double chance,
        @Nullable Double depthBias
) {

    public static final ProfileDecaySettings DISABLED = new ProfileDecaySettings(false, null, null, null, null);

    public static final int DEFAULT_GRACE_SECONDS = 30;
    public static final int DEFAULT_RAMP_SECONDS = 30;
    public static final double DEFAULT_CHANCE = 0.04D;
    public static final double DEFAULT_DEPTH_BIAS = 0.5D;

    public int graceSecondsOrDefault() {
        return graceSeconds != null ? graceSeconds : DEFAULT_GRACE_SECONDS;
    }

    public int rampSecondsOrDefault() {
        return rampSeconds != null ? rampSeconds : DEFAULT_RAMP_SECONDS;
    }

    public double chanceOrDefault() {
        return chance != null ? chance : DEFAULT_CHANCE;
    }

    public double depthBiasOrDefault() {
        return depthBias != null ? depthBias : DEFAULT_DEPTH_BIAS;
    }

    public ProfileDecaySettings withEnabled(boolean value) {
        return new ProfileDecaySettings(value, graceSeconds, rampSeconds, chance, depthBias);
    }

    public ProfileDecaySettings withGraceSeconds(@Nullable Integer value) {
        return new ProfileDecaySettings(enabled, value, rampSeconds, chance, depthBias);
    }

    public ProfileDecaySettings withRampSeconds(@Nullable Integer value) {
        return new ProfileDecaySettings(enabled, graceSeconds, value, chance, depthBias);
    }

    public ProfileDecaySettings withChance(@Nullable Double value) {
        return new ProfileDecaySettings(enabled, graceSeconds, rampSeconds, value, depthBias);
    }

    public ProfileDecaySettings withDepthBias(@Nullable Double value) {
        return new ProfileDecaySettings(enabled, graceSeconds, rampSeconds, chance, value);
    }

}
