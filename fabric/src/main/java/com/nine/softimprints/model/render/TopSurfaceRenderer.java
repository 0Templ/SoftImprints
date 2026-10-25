package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.ImprintStrip;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;

import java.util.ArrayList;
import java.util.List;

public class TopSurfaceRenderer implements ImprintSurfaceRenderer {

    private static TextureAtlasSprite zeroLayerSprite(net.minecraft.resources.Identifier texture) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
        return atlas.getSprite(texture);
    }

    @Override
    public void emit(ImprintRenderContext context) {
        var textureSets = context.profile().textureSets();
        var set = textureSets.getCurrent();
        if (set == null) {
            context.wrapped().emitQuads(context.emitter(), context.level(), context.pos(), context.state(),
                    context.random(), context.cullTest());
            return;
        }

        // Keep this mesh local: connected textures depend on neighboring blocks and resource reloads.
        var mesh = Renderer.get().mutableMesh();
        context.wrapped().emitQuads(mesh.emitter(), context.level(), context.pos(), context.state(),
                context.random(), context.cullTest());

        List<ImprintStrip> strips = new ArrayList<>();
        ImprintStrip.consume(strips::add, context.map());
        TextureAtlasSprite zeroLayer = context.surface().useOriginalZeroLayer()
                ? null : zeroLayerSprite(textureSets.zeroLayer());
        int mapSize = context.map().size();
        var emitter = context.emitter();

        mesh.forEach(quad -> {
            TopFaceClip clip = TopFaceClip.analyze(quad);
            if (clip == null) {
                emitter.copyFrom(quad).emit();
                return;
            }

            for (ImprintStrip strip : strips) {
                float x0 = strip.x0() / (float) mapSize;
                float x1 = strip.x1() / (float) mapSize;
                float z0 = strip.y0() / (float) mapSize;
                float z1 = (strip.y0() + 1) / (float) mapSize;
                if (!clip.crop(quad, emitter, x0, z0, x1, z1)) continue;

                TextureAtlasSprite replacement = strip.value() == 0 ? zeroLayer : set.spriteFor(strip.value());
                if (replacement != null) {
                    for (int i = 0; i < 4; i++) {
                        EmitHelper.setUvRotated(emitter, replacement, i, emitter.x(i), emitter.z(i),
                                context.blockRenderData().rotation());
                    }
                    emitter.postMaterialBake(new Material.Baked(replacement, false));
                }
                // Missing imprint textures preserve the original surface instead of creating holes.
                emitter.emit();
            }
        });
    }
}
