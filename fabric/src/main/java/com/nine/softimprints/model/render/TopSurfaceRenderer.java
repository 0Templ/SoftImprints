package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.ImprintStrip;
import com.nine.softimprints.model.ModelUtils;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class TopSurfaceRenderer implements ImprintSurfaceRenderer{

    private static final QuadTransform DROP_TOP_FACE = TopSurfaceRenderer::keepOnlySidesAndBottom;

    @Override
    public void emit(ImprintRenderContext context) {
        var emitter = context.emitter();

        try {
            emitter.pushTransform(DROP_TOP_FACE);
            context.wrapped().emitQuads(
                    emitter,
                    context.level(),
                    context.pos(),
                    context.state(),
                    context.random(),
                    context.cullTest()
            );

        } finally {
            emitter.popTransform();
        }
        float topY = ModelUtils.resolveTopY(context.state(), context.level(), context.pos());
        float depth = 1.0F - topY;

        var entry = context.blockRenderData();

        var textureSets = context.profile().textureSets();
        var zeroLayer = context.surface().useOriginalZeroLayer()
                ? entry.topSprite()
                : zeroLayerSprite(textureSets.zeroLayer());
        var set = textureSets.getCurrent();
        if (set == null) return;

        long seed = context.state().getSeed(context.pos());

        context.random().setSeed(seed);
        List<BlockStateModelPart> parts = new ArrayList<>();
        context.wrapped().collectParts(context.random(), parts);

        ImprintStrip.consume(strip -> {
            byte value = strip.value();
            TextureAtlasSprite texture;
            if (value == 0) {
                texture = zeroLayer;
            }
            else {
                texture = set.spriteFor(value);
                if (texture == null) return;
            }
            EmitHelper.emitStrip(emitter, texture, strip, context.map().size(), depth, entry.rotation());
        }, context.map());

    }

    private static boolean keepOnlySidesAndBottom(MutableQuadView quad) {
        return quad.nominalFace() != Direction.UP;
    }

    private static TextureAtlasSprite zeroLayerSprite(net.minecraft.resources.Identifier texture) {
        TextureAtlas atlas = (TextureAtlas) Minecraft.getInstance()
                .getTextureManager()
                .getTexture(TextureAtlas.LOCATION_BLOCKS);
        return atlas.getSprite(texture);
    }

}
