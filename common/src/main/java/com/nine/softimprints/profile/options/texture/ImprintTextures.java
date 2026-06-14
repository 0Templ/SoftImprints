package com.nine.softimprints.profile.options.texture;

import net.minecraft.resources.Identifier;

import java.util.*;

public record ImprintTextures(
        String selected,
        Identifier zeroLayer,
        Map<String, ImprintTextureSet> map
) {

    public ImprintTextures {
        Objects.requireNonNull(selected, "selected");
        zeroLayer = Objects.requireNonNull(zeroLayer, "zeroLayer");
        map = Collections.unmodifiableMap(new LinkedHashMap<>(map));
    }

    public static ImprintTextures fromSets(
            String selected,
            Identifier initLayer,
            Iterable<? extends ImprintTextureSet> sets
    ) {
        Objects.requireNonNull(sets, "sets");
        Map<String, ImprintTextureSet> collected = new LinkedHashMap<>();
        if (selected == null) throw new IllegalArgumentException("Selected set id is not set");
        if (selected.isBlank()) throw new IllegalArgumentException("Selected set id is blank");
        for (ImprintTextureSet set : sets) {
            if (set == null) {
                throw new IllegalArgumentException("Texture set cannot be null");
            }
            String id = Objects.requireNonNull(set.id(), "Texture set selected cannot be null");
            if (id.isBlank()) {
                throw new IllegalArgumentException("Texture set selected cannot be blank");
            }
            if (collected.putIfAbsent(id, set) != null) {
                throw new IllegalArgumentException("Duplicate texture set selected: " + id);
            }
        }
        if (!collected.containsKey(selected)) {
            throw new IllegalArgumentException("Wrong current texture set Id: " + selected);
        }
        return new ImprintTextures(selected, initLayer, collected);
    }

    public ImprintTextureSet getCurrent() {
        return map.get(selected);
    }

    public ImprintTextures withSelected(String selected) {
        if (selected == null) {
            throw new IllegalArgumentException("Selected set id is not set");
        }
        if (!map.containsKey(selected)) {
            throw new IllegalArgumentException("Wrong current texture set Id: " + selected);
        }
        return new ImprintTextures(selected, zeroLayer, map);
    }

    public ImprintTextures selectNext() {
        if (map.isEmpty()) {
            return this;
        }

        List<String> ids = List.copyOf(map.keySet());
        int currentIndex = ids.indexOf(selected);
        int nextIndex = currentIndex < 0 ? 0 : (currentIndex + 1) % ids.size();
        return withSelected(ids.get(nextIndex));
    }

}
