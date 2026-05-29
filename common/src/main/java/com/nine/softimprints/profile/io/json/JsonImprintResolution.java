package com.nine.softimprints.profile.io.json;

import com.google.gson.annotations.SerializedName;

public record JsonImprintResolution(
        @SerializedName("map_size")
        Integer mapSize
) {

    public JsonImprintResolution merge(JsonImprintResolution with) {
        if (with == null) return this;
        return new JsonImprintResolution(
                with.mapSize != null ? with.mapSize : this.mapSize
        );
    }
}
