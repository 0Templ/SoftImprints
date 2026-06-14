package com.nine.softimprints.profile.options.layer;

import com.google.gson.annotations.SerializedName;

public record ImprintLayer(
        byte value,
        boolean enable,
        int expand,
        @SerializedName("inner_jitter")
        float innerJitter,
        @SerializedName("outer_jitter")
        float outerJitter,
        float erosion
) {

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {

        private final byte value;
        private boolean enable;
        private int expand;
        private float outerJitter;
        private float innerJitter;
        private float erosion;

        private Builder(ImprintLayer src) {
            this.value = src.value;
            this.enable = src.enable;
            this.expand = src.expand;
            this.outerJitter = src.outerJitter;
            this.innerJitter = src.innerJitter;
            this.erosion = src.erosion;
        }

        public Builder setExpand(int v) {
            this.expand = v;
            return this;
        }

        public Builder setEnabled(boolean value) {
            this.enable = value;
            return this;
        }

        public Builder setInnerJitter(float v) {
            this.innerJitter = v;
            return this;
        }

        public Builder setOuterJitter(float v) {
            this.outerJitter = v;
            return this;
        }

        public Builder setErosion(float v) {
            this.erosion = v;
            return this;
        }

        public ImprintLayer build() {
            return new ImprintLayer(value, enable, expand, innerJitter, outerJitter, erosion);
        }
    }

}

