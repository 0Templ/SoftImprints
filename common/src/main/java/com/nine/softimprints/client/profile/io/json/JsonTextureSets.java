package com.nine.softimprints.client.profile.io.json;

import java.util.Map;
import java.util.Objects;

public record JsonTextureSets(
        String selected,
        String initLayer,
        Boolean useOriginalZeroLayer,
        Map<String, Map<String, String>> texturesByValue
) {

        public JsonTextureSets merge(JsonTextureSets with) {
                if (with == null) return this;

                return new JsonTextureSets(
                        with.selected != null ? with.selected : this.selected,
                        with.initLayer != null ? with.initLayer : this.initLayer,
                        with.useOriginalZeroLayer != null ? with.useOriginalZeroLayer : this.useOriginalZeroLayer,
                        with.texturesByValue != null ? with.texturesByValue : this.texturesByValue
                );
        }

        public JsonTextureSets nullifyAgainst(JsonTextureSets with) {
                if (with == null) return this;

                return new JsonTextureSets(
                        Objects.equals(with.selected, this.selected) ? null : this.selected,
                        Objects.equals(with.initLayer, this.initLayer) ? null : this.initLayer,
                        Objects.equals(with.useOriginalZeroLayer, this.useOriginalZeroLayer) ? null : this.useOriginalZeroLayer,
                        Objects.equals(with.texturesByValue, this.texturesByValue) ? null : this.texturesByValue
                );
        }

}
