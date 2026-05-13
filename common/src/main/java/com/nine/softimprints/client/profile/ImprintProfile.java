package com.nine.softimprints.client.profile;

import com.nine.softimprints.client.profile.options.block.SurfaceBlock;
import com.nine.softimprints.client.profile.options.layer.ImprintInitLayer;
import com.nine.softimprints.client.profile.options.layer.ImprintLayer;
import com.nine.softimprints.client.profile.options.resoltuion.ImprintResolution;
import com.nine.softimprints.client.profile.options.surface.ImprintSurfaceSettings;
import com.nine.softimprints.client.profile.options.texture.ImprintTextureSets;
import com.nine.softimprints.client.profile.util.ProfilesHelper;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.UnaryOperator;

public final class ImprintProfile {

    public final Identifier id;
    public final List<ImprintLayer> layers;

    public final Set<SurfaceBlock> supportedBlocks;

    public final ImprintSurfaceSettings surface;

    public final ImprintTextureSets textureSets;

    public final ImprintResolution resolution;

    public ImprintProfile(
            Identifier id,
            List<ImprintLayer> layers,
            Set<SurfaceBlock> supportedBlocks,
            ImprintTextureSets textureSets,
            ImprintResolution resolution
    ) {
        this(id, layers, supportedBlocks, ImprintSurfaceSettings.DEFAULT, textureSets, resolution);
    }

    public ImprintProfile(
            Identifier id,
            List<ImprintLayer> layers,
            Set<SurfaceBlock> supportedBlocks,
            ImprintSurfaceSettings surface,
            ImprintTextureSets textureSets,

            ImprintResolution resolution
    ) {
        this.id = id;
        this.layers = List.copyOf(Objects.requireNonNull(layers, "layers")).stream()
                .sorted(Comparator.comparingInt(ImprintLayer::value))
                .toList();
        this.supportedBlocks = Set.copyOf(supportedBlocks);
        this.surface = Objects.requireNonNull(surface, "surface");
        this.textureSets = Objects.requireNonNull(textureSets, "textureSets");


        this.resolution = resolution;

        ProfilesHelper.validateLayersAndTextures(this);
    }

    public ImprintProfile copy() {
        return new ImprintProfile(
                this.id,
                new ArrayList<>(this.layers),
                new HashSet<>(this.supportedBlocks),
                this.surface,
                this.textureSets,
                this.resolution
        );
    }

    public List<ImprintLayer> getLayers() {
        return this.layers;
    }

    public Set<SurfaceBlock> supportedBlocks() {
        return this.supportedBlocks;
    }

    public ImprintTextureSets textureSets() {
        return this.textureSets;
    }

    public ImprintSurfaceSettings surface() {
        return this.surface;
    }

    public Identifier id() {
        return this.id;
    }

    public ImprintResolution resolution() {
        return this.resolution;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ImprintProfile other)) return false;
        return this.id.equals(other.id)
                && this.supportedBlocks.equals(other.supportedBlocks)
                && this.surface.equals(other.surface)
                && this.textureSets.equals(other.textureSets)
                && this.layers.equals(other.layers)
                && this.resolution == other.resolution()

                ;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static final class Builder {

        private final Identifier id;
        private List<ImprintLayer> layers;
        private ImprintInitLayer initLayer;
        private Set<SurfaceBlock> supportedBlocks;
        private ImprintSurfaceSettings surface;
        private ImprintTextureSets textureSets;
        private ImprintResolution resolution;

        private Builder(ImprintProfile src) {
            this.id = src.id;
            this.layers = new ArrayList<>(src.layers);
            this.supportedBlocks = new HashSet<>(src.supportedBlocks);
            this.surface = src.surface;
            this.textureSets = src.textureSets;
            this.resolution = src.resolution;
        }

        public Builder setLayers(List<ImprintLayer> layers) {
            this.layers = new ArrayList<>(layers);
            return this;
        }

        public Builder replaceLayer(int index, ImprintLayer layer) {
            this.layers.set(index, layer);
            return this;
        }

        public Builder mutateLayer(byte value, UnaryOperator<ImprintLayer> fn) {
            for (int i = 0; i < this.layers.size(); i++) {
                ImprintLayer layer = this.layers.get(i);
                if (layer.value() == value) {
                    this.layers.set(i, fn.apply(layer));
                    return this;
                }
            }
            return this;
        }

        public Builder mutateLayer(int index, UnaryOperator<ImprintLayer> fn) {
            this.layers.set(index, fn.apply(this.layers.get(index)));
            return this;
        }

        public Builder mutateInitLayer(UnaryOperator<ImprintInitLayer> fn) {
            this.initLayer = fn.apply(this.initLayer);
            return this;
        }

        public Builder setSupportedBlocks(Set<SurfaceBlock> blocks) {
            this.supportedBlocks = new HashSet<>(blocks);
            return this;
        }

        public Builder setTextureSets(ImprintTextureSets sets) {
            this.textureSets = sets;
            return this;
        }

        public Builder setSurface(ImprintSurfaceSettings surface) {
            this.surface = surface;
            return this;
        }

        public Builder mutateSurface(UnaryOperator<ImprintSurfaceSettings> fn) {
            this.surface = fn.apply(this.surface);
            return this;
        }

        public Builder mutateTextureSets(UnaryOperator<ImprintTextureSets> fn) {
            this.textureSets = fn.apply(this.textureSets);
            return this;
        }

        public Builder setResolution(UnaryOperator<ImprintResolution> fn) {
            this.resolution = fn.apply(this.resolution);
            return this;
        }

        public ImprintProfile build() {
            return new ImprintProfile(id, layers, supportedBlocks, surface, textureSets, resolution);
        }
    }

}
