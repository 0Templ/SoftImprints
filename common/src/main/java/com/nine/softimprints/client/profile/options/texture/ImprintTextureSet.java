package com.nine.softimprints.client.profile.options.texture;

import com.nine.softimprints.SICommon;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class ImprintTextureSet {

    public static final ImprintTextureSet DEBUG_SET = new ImprintTextureSet(
            "debug",
            Map.of(
                    (byte) 1, Identifier.fromNamespaceAndPath(SICommon.MODID,
                            "block/imprints/debug/sets/debug_layer_1"),
                    (byte) 2, Identifier.fromNamespaceAndPath(SICommon.MODID,
                            "block/imprints/debug/sets/debug_layer_2"),
                    (byte) 3, Identifier.fromNamespaceAndPath(SICommon.MODID,
                            "block/imprints/debug/sets/debug_layer_3"),
                    (byte) 4, Identifier.fromNamespaceAndPath(SICommon.MODID,
                            "block/imprints/debug/sets/debug_layer_4"),
                    (byte) 5, Identifier.fromNamespaceAndPath(SICommon.MODID,
                            "block/imprints/debug/sets/debug_layer_5")
            )
    );

    private final String id;
    private final Map<Byte, Identifier> byValue;

    // Todo: move to external cache class?
    private volatile TextureAtlasSprite[] spriteCache;

    public ImprintTextureSet(String id, Map<Byte, Identifier> byValue){
        this.id = Objects.requireNonNull(id, "id");
        this.byValue = Collections.unmodifiableMap(new LinkedHashMap<>(byValue));
        for (Byte value : this.byValue.keySet()) {
            if (value == null) {
                throw new IllegalArgumentException("Texture value cannot be null");
            }
            if (value <= 0) {
                throw new IllegalArgumentException("Texture value must be in range 1..127: " + value);
            }
        }
    }

    public String id(){
        return id;
    }

    public Map<Byte, Identifier> texturesByValue() {
        return byValue;
    }

    @Nullable
    public TextureAtlasSprite spriteFor(byte value){
        var cache = spriteCache;
        if (cache == null){
            cache = bind();
        }
        int index = value & 0xFF;
        return index >= cache.length ? null : cache[index];
    }

    private synchronized TextureAtlasSprite[] bind(){
        if (byValue.isEmpty()) return spriteCache = new TextureAtlasSprite[0];
        
        var current = spriteCache;
        if (current != null) return current;

        int max = 0;
        for (var b : byValue.entrySet()){
            int val = b.getKey() & 0xFF;
            if (val > max) max = val;
        }
        var ret = new TextureAtlasSprite[max + 1];
        var atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        for (var b : byValue.entrySet()){
            int index = b.getKey() & 0xFF;
            ret[index] = atlas.getSprite(b.getValue());
        }
        return spriteCache = ret;
    }

    public void invalidate(){
        this.spriteCache = null;
    }

}