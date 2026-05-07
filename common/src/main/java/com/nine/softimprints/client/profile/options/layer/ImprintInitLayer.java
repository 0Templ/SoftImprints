package com.nine.softimprints.client.profile.options.layer;

public record ImprintInitLayer(
        byte value,
        float jitter,
        float erosion
){

    public Builder toBuilder() { return new Builder(this); }

    public static final class Builder {

        private final byte value;
        private float jitter;
        private float erosion;

        private Builder(ImprintInitLayer src) {
            this.value = src.value;
            this.jitter = src.jitter;
            this.erosion = src.erosion;
        }

        public Builder setJitter(float v) { this.jitter = v; return this; }
        public Builder setErosion(float v) { this.erosion = v; return this; }

        public ImprintInitLayer build() { return new ImprintInitLayer(value, jitter, erosion); }
    }

}

