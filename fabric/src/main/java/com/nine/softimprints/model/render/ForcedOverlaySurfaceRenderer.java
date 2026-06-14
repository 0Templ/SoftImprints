package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.ImprintStrip;

public final class ForcedOverlaySurfaceRenderer {

    private ForcedOverlaySurfaceRenderer() {
    }

    public static void emit(
            ImprintRenderContext context,
            float forcedY
    ) {
        var emitter = context.emitter();
        context.wrapped().emitQuads(
                emitter,
                context.level(),
                context.pos(),
                context.state(),
                context.random(),
                context.cullTest()
        );

        var set = context.profile().textureSets().getCurrent();
        if (set == null) return;

        float depth = 1.0F - forcedY;
        int mapSize = context.map().size();
        int rotation = context.blockRenderData().rotation();

        ImprintStrip.consume(strip -> {
            byte value = strip.value();
            if (value <= 0) return;
            var sprite = set.spriteFor(value);
            if (sprite == null) return;
            EmitHelper.emitStrip(emitter, sprite, strip, mapSize, depth, rotation);
        }, context.map());
    }
}
