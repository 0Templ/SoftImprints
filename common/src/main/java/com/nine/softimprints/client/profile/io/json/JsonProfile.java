package com.nine.softimprints.client.profile.io.json;

import com.nine.softimprints.client.profile.options.layer.ImprintLayer;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record JsonProfile(
        int version,
        List<ImprintLayer> layers,
        Set<String> supportedBlocks,
        JsonSurfaceSettings surface,
        JsonTextureSets textureSets
) {

    public static final String SCHEMA_KEY = "version";
    public static final int CURRENT_SCHEMA = 1;

    public JsonProfile merge(JsonProfile with){
        return new JsonProfile(this.version,
                with.layers() != null ? with.layers() : this.layers(),
                with.supportedBlocks() != null ? with.supportedBlocks() : this.supportedBlocks(),
                with.surface() != null ? mergeSurface(with.surface()) : this.surface,
                with.textureSets() != null ? this.textureSets().merge(with.textureSets) : this.textureSets
        );
    }

    public JsonProfile nullifyAgainst(JsonProfile against){
        return new JsonProfile(
                this.version,
                Objects.equals(against.layers, this.layers) ? null : this.layers,
                Objects.equals(against.supportedBlocks, this.supportedBlocks) ? null : this.supportedBlocks,
                Objects.equals(against.surface, this.surface) ? null : nullifySurface(against.surface),
                Objects.equals(against.textureSets, this.textureSets) ? null : this.textureSets
        );
    }

    public boolean isEmpty(){
        return layers == null
                && supportedBlocks == null
                && surface == null
                && textureSets == null;
    }

    private JsonSurfaceSettings mergeSurface(JsonSurfaceSettings with) {
        return this.surface != null ? this.surface.merge(with) : with;
    }

    private JsonSurfaceSettings nullifySurface(JsonSurfaceSettings against) {
        return this.surface != null ? this.surface.nullifyAgainst(against) : null;
    }

}
