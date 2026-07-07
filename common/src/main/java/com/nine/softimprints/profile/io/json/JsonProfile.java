package com.nine.softimprints.profile.io.json;

import com.google.gson.annotations.SerializedName;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.options.layer.ImprintLayer;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record JsonProfile(
        int version,
        List<ImprintLayer> layers,
        @SerializedName("supported_blocks")
        Set<String> supportedBlocks,
        @SerializedName("surface")
        JsonSurfaceSettings surface,
        @SerializedName("texture_sets")
        JsonTextureSets textureSets,
        @SerializedName("resolution")
        JsonImprintResolution resolution,

        @SerializedName("preview")
        JsonImprintPreviewAssets preview,
        @SerializedName("decay")
        JsonDecaySettings decay,
        Integer priority
) {

    public static final String SCHEMA_KEY = "version";
    public static final int CURRENT_SCHEMA = 3;

    private static final JsonDecaySettings DISABLED_DECAY = new JsonDecaySettings(false, null, null, null, null);

    public JsonProfile merge(JsonProfile with) {
        return new JsonProfile(
                this.version,
                with.layers() != null ? with.layers() : this.layers(),
                with.supportedBlocks() != null ? with.supportedBlocks() : this.supportedBlocks(),
                with.surface() != null ? mergeSurface(with.surface()) : this.surface(),
                with.textureSets() != null ? this.textureSets().merge(with.textureSets()) : this.textureSets(),
                with.resolution() != null ? mergeResolution(with.resolution()) : this.resolution(),
                this.preview,
                with.decay() != null ? mergeDecay(with.decay()) : this.decay(),
                with.priority() != null ? with.priority() : this.priority
        );
    }

    public JsonProfile nullifyAgainst(JsonProfile against) {
        return new JsonProfile(
                this.version,
                Objects.equals(against.layers, this.layers) ? null : this.layers,
                Objects.equals(against.supportedBlocks, this.supportedBlocks) ? null : this.supportedBlocks,
                Objects.equals(against.surface, this.surface) ? null : nullifySurface(against.surface),
                Objects.equals(against.textureSets, this.textureSets) ? null : this.textureSets,
                Objects.equals(against.resolution, this.resolution) ? null : this.resolution,
                null,
                decayEquals(against.decay, this.decay) ? null : this.decay,
                priorityEquals(against.priority, this.priority) ? null : this.priority
        );
    }

    public boolean isEmpty() {
        return layers == null
                && supportedBlocks == null
                && surface == null
                && textureSets == null
                && decay == null
                && priority == null;
    }

    private static boolean priorityEquals(
            Integer against,
            Integer value
    ) {
        int a = against != null ? against : ImprintProfile.DEFAULT_PRIORITY;
        int b = value != null ? value : ImprintProfile.DEFAULT_PRIORITY;
        return a == b;
    }

    private static boolean decayEquals(
            JsonDecaySettings against,
            JsonDecaySettings value
    ) {
        JsonDecaySettings a = against != null ? against : DISABLED_DECAY;
        JsonDecaySettings b = value != null ? value : DISABLED_DECAY;
        return Objects.equals(a, b);
    }

    private JsonImprintResolution mergeResolution(JsonImprintResolution with) {
        return this.resolution() == null
                ? with
                : this.resolution().merge(with);
    }

    private JsonDecaySettings mergeDecay(JsonDecaySettings with) {
        return this.decay() == null
                ? with
                : this.decay().merge(with);
    }

    private JsonSurfaceSettings mergeSurface(JsonSurfaceSettings with) {
        return this.surface != null ? this.surface.merge(with) : with;
    }

    private JsonSurfaceSettings nullifySurface(JsonSurfaceSettings against) {
        return this.surface != null ? this.surface.nullifyAgainst(against) : null;
    }

}
