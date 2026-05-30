package com.nine.softimprints.temp.model.render;

import com.nine.softimprints.core.map.ImprintStrip;
import net.minecraft.client.resources.model.geometry.BakedQuad;

import java.util.ArrayList;
import java.util.List;

public final class ForcedOverlaySurfaceRenderer {

    private static final float SURFACE_OFFSET = 1.0F / 4096.0F;

    public static void emit(ImprintRenderContext context, float forcedY) {
        var set = context.profile().textureSets().getCurrent();
        if (set == null) return;

        int mapSize = context.map().size();
        int rotation = context.blockRenderData().rotation();
        var particle = context.blockRenderData().topSprite();
        float quadY = forcedY + SURFACE_OFFSET;

        List<BakedQuad> quads = new ArrayList<>();
        ImprintStrip.consume(strip -> {
            byte value = strip.value();
            if (value <= 0) return;
            var sprite = set.spriteFor(value);
            if (sprite == null) return;
            quads.add(EmitHelper.buildStripQuad(sprite, strip, mapSize, quadY, rotation));
        }, context.map());

        if (!quads.isEmpty()) {
            context.parts().add(ImprintParts.staticUpPart(quads, particle));
        }
    }
}
