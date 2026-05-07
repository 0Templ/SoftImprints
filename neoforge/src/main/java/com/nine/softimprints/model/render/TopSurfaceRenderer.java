package com.nine.softimprints.model.render;

import com.nine.softimprints.client.core.map.ImprintStrip;
import com.nine.softimprints.client.model.ModelUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * NeoForge counterpart of the fabric TOP renderer: removes the wrapped block's top face and
 * replaces it entirely with imprint strips. Strips with value 0 fall back to either the
 * block's original top sprite or a profile-supplied "zero layer" sprite.
 */
public class TopSurfaceRenderer implements ImprintSurfaceRenderer {

    @Override
    public void emit(ImprintRenderContext context) {
        // The wrapped model has already pushed parts into context.parts() via super.collectParts.
        // Replace each part with one whose UP-facing quads are stripped — the imprint strips
        // below take over that surface.
        List<BlockStateModelPart> parts = context.parts();
        for (int i = 0; i < parts.size(); i++) {
            parts.set(i, ImprintParts.withoutUpQuads(parts.get(i)));
        }

        var textureSets = context.profile().textureSets();
        var set = textureSets.getCurrent();
        if (set == null) return;

        var entry = context.blockRenderData();
        TextureAtlasSprite zeroLayer = context.surface().useOriginalZeroLayer()
                ? entry.topSprite()
                : zeroLayerSprite(textureSets.zeroLayer());

        float topY = ModelUtils.resolveTopY(context.state(), context.level(), context.pos());
        int mapSize = context.map().size();
        int rotation = entry.rotation();

        List<BakedQuad> quads = new ArrayList<>();
        ImprintStrip.consume(strip -> {
            byte value = strip.value();
            TextureAtlasSprite sprite;
            if (value == 0) {
                sprite = zeroLayer;
            } else {
                sprite = set.spriteFor(value);
                if (sprite == null) return;
            }
            quads.add(EmitHelper.buildStripQuad(sprite, strip, mapSize, topY, rotation));
        }, context.map());

        if (!quads.isEmpty()) {
            parts.add(ImprintParts.staticUpPart(quads, entry.topSprite()));
        }
    }

    private static TextureAtlasSprite zeroLayerSprite(Identifier texture) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
        return atlas.getSprite(texture);
    }
}
