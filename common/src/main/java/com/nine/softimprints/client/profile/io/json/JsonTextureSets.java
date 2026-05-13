package com.nine.softimprints.client.profile.io.json;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Objects;

public record JsonTextureSets(
        String selected,
        @SerializedName("init_layer")
        String initLayer,
        @SerializedName("textures_by_value")
        Map<String, Map<String, String>> texturesByValue
) {

        public JsonTextureSets merge(JsonTextureSets with) {
                if (with == null) return this;

                return new JsonTextureSets(
                        with.selected != null ? with.selected : this.selected,
                        with.initLayer != null ? with.initLayer : this.initLayer,
                        with.texturesByValue != null ? with.texturesByValue : this.texturesByValue
                );
        }

        public JsonTextureSets nullifyAgainst(JsonTextureSets with) {
                if (with == null) return this;

                return new JsonTextureSets(
                        Objects.equals(with.selected, this.selected) ? null : this.selected,
                        Objects.equals(with.initLayer, this.initLayer) ? null : this.initLayer,
                        Objects.equals(with.texturesByValue, this.texturesByValue) ? null : this.texturesByValue
                );
        }

}
