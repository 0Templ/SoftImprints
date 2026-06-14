package com.nine.softimprints.model.render;

import com.nine.softimprints.core.map.ImprintStrip;
import com.nine.softimprints.model.ModelUtils;

public class OverlaySurfaceRenderer implements ImprintSurfaceRenderer {

    private static final float SURFACE_OFFSET = 1.0F / 4096.0F;

    @Override
    public void emit(ImprintRenderContext context) {
        var emitter = context.emitter();
        context.wrapped().emitQuads(
                emitter,
                context.level(),
                context.pos(),
                context.state(),
                context.random(),
                context.cullTest()
        );

        float topY = ModelUtils.resolveTopY(context.state(), context.level(), context.pos());
        float depth = 1.0F - topY - SURFACE_OFFSET;

        var set = context.profile().textureSets().getCurrent();
        if (set == null) return;

        ImprintStrip.consume(strip -> {
            byte value = strip.value();
            if (value <= 0) return;
            var texture = set.spriteFor(value);
            if (texture == null) return;
            EmitHelper.emitStrip(emitter, texture, strip, context.map().size(), depth, context.blockRenderData().rotation());
        }, context.map());

    }


}
