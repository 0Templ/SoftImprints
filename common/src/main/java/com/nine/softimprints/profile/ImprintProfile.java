package com.nine.softimprints.profile;

import com.nine.softimprints.profile.options.ImprintPreviewAssets;
import com.nine.softimprints.profile.options.block.SurfaceBlock;
import com.nine.softimprints.profile.options.decay.ProfileDecaySettings;
import com.nine.softimprints.profile.options.layer.ImprintLayer;
import com.nine.softimprints.profile.options.resolution.ImprintResolution;
import com.nine.softimprints.profile.options.surface.SurfaceSettings;
import com.nine.softimprints.profile.options.texture.ImprintTextures;
import com.nine.softimprints.profile.util.ProfilesHelper;
import net.minecraft.resources.Identifier;

import java.util.*;
import java.util.function.UnaryOperator;

public final class ImprintProfile {

    public static final int DEFAULT_PRIORITY = 100;

    public final Identifier id;
    public final List<ImprintLayer> layers;

    public final Set<SurfaceBlock> supportedBlocks;

    public final SurfaceSettings surface;

    public final ImprintTextures textureSets;

    public final ImprintResolution resolution;

    public final ImprintPreviewAssets preview;

    public final ProfileDecaySettings decay;

    public final int priority;

    public ImprintProfile(
            Identifier id,
            List<ImprintLayer> layers,
            Set<SurfaceBlock> supportedBlocks,
            ImprintTextures textureSets,
            ImprintResolution resolution,
            ImprintPreviewAssets preview,
            int priority
    ) {
        this(id, layers, supportedBlocks, SurfaceSettings.DEFAULT, textureSets, resolution, preview, ProfileDecaySettings.DISABLED, priority);
    }

    public ImprintProfile(
            Identifier id,
            List<ImprintLayer> layers,
            Set<SurfaceBlock> supportedBlocks,
            SurfaceSettings surface,
            ImprintTextures textureSets,
            ImprintResolution resolution,
            ImprintPreviewAssets preview,
            ProfileDecaySettings decay,
            int priority
    ) {
        this.id = id;
        this.layers = List.copyOf(Objects.requireNonNull(layers, "layers")).stream()
                .sorted(Comparator.comparingInt(ImprintLayer::value))
                .toList();
        this.supportedBlocks = Set.copyOf(supportedBlocks);
        this.surface = Objects.requireNonNull(surface, "surface");
        this.textureSets = Objects.requireNonNull(textureSets, "textureSets");


        this.resolution = resolution;
        this.preview = preview;
        this.decay = Objects.requireNonNull(decay, "decay");
        this.priority = priority;

        ProfilesHelper.validateLayersAndTextures(this);
    }

    public ImprintProfile copy() {
        return new ImprintProfile(
                this.id,
                new ArrayList<>(this.layers),
                new HashSet<>(this.supportedBlocks),
                this.surface,
                this.textureSets,
                this.resolution,
                this.preview,
                this.decay,
                this.priority
        );
    }

    public List<ImprintLayer> getLayers() {
        return this.layers;
    }

    public Set<SurfaceBlock> supportedBlocks() {
        return this.supportedBlocks;
    }

    public ImprintTextures textureSets() {
        return this.textureSets;
    }

    public SurfaceSettings surface() {
        return this.surface;
    }

    public Identifier id() {
        return this.id;
    }

    public ImprintResolution resolution() {
        return this.resolution;
    }

    public ImprintPreviewAssets preview() {
        return this.preview;
    }

    public ProfileDecaySettings decay() {
        return this.decay;
    }

    public int priority() {
        return this.priority;
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

                && this.preview == other.preview()
                && this.decay.equals(other.decay())
                && this.priority == other.priority()

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

        public final ImprintPreviewAssets preview;
        private final Identifier id;
        private int priority;
        private List<ImprintLayer> layers;
        private Set<SurfaceBlock> supportedBlocks;
        private SurfaceSettings surface;
        private ImprintTextures textureSets;
        private ImprintResolution resolution;
        private ProfileDecaySettings decay;

        private Builder(ImprintProfile src) {
            this.id = src.id;
            this.layers = new ArrayList<>(src.layers);
            this.supportedBlocks = new HashSet<>(src.supportedBlocks);
            this.surface = src.surface;
            this.textureSets = src.textureSets;
            this.resolution = src.resolution;
            this.decay = src.decay;

            this.preview = src.preview;
            this.priority = src.priority;
        }

        public Builder setLayers(List<ImprintLayer> layers) {
            this.layers = new ArrayList<>(layers);
            return this;
        }

        public Builder replaceLayer(
                int index,
                ImprintLayer layer
        ) {
            this.layers.set(index, layer);
            return this;
        }

        public Builder mutateLayer(
                byte value,
                UnaryOperator<ImprintLayer> fn
        ) {
            for (int i = 0; i < this.layers.size(); i++) {
                ImprintLayer layer = this.layers.get(i);
                if (layer.value() == value) {
                    this.layers.set(i, fn.apply(layer));
                    return this;
                }
            }
            return this;
        }

        public Builder mutateLayer(
                int index,
                UnaryOperator<ImprintLayer> fn
        ) {
            this.layers.set(index, fn.apply(this.layers.get(index)));
            return this;
        }

        public Builder setSupportedBlocks(Set<SurfaceBlock> blocks) {
            this.supportedBlocks = new HashSet<>(blocks);
            return this;
        }

        public Builder setTextureSets(ImprintTextures sets) {
            this.textureSets = sets;
            return this;
        }

        public Builder setSurface(SurfaceSettings surface) {
            this.surface = surface;
            return this;
        }

        public Builder mutateSurface(UnaryOperator<SurfaceSettings> fn) {
            this.surface = fn.apply(this.surface);
            return this;
        }

        public Builder mutateTextureSets(UnaryOperator<ImprintTextures> fn) {
            this.textureSets = fn.apply(this.textureSets);
            return this;
        }

        public Builder mutateResolution(UnaryOperator<ImprintResolution> fn) {
            this.resolution = fn.apply(this.resolution);
            return this;
        }

        public Builder setResolution(ImprintResolution value) {
            this.resolution = value;
            return this;
        }

        public Builder setDecay(ProfileDecaySettings value) {
            this.decay = value;
            return this;
        }

        public Builder setPriority(int value) {
            this.priority = value;
            return this;
        }

        public ImprintProfile build() {
            return new ImprintProfile(id, layers, supportedBlocks, surface, textureSets, resolution, preview, decay, priority);
        }
    }

}
