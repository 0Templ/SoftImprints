package com.nine.softimprints.client.profile.io.json;

import com.google.gson.annotations.SerializedName;

public record JsonImprintResolution(
        @SerializedName("map_size")
        Integer mapSize,
        @SerializedName("texture_size")
        Integer textureSize
) {

    public JsonImprintResolution merge(JsonImprintResolution with) {
        if (with == null) return this;
        return new JsonImprintResolution(
                with.mapSize != null ? with.mapSize : this.mapSize,
                with.textureSize != null ? with.textureSize : this.textureSize
        );
    }
}