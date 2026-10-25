package com.nine.softimprints.core.contact.model.capture;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import java.util.List;

public final class ModelPoseGuard implements AutoCloseable {
    private record Saved(ModelPart part, PartPose pose, float sx, float sy, float sz, boolean visible, boolean skipDraw) {}
    private final List<Saved> saved;
    public ModelPoseGuard(ModelPart root) {
        saved = root.getAllParts().stream().map(part -> new Saved(part, part.storePose(), part.xScale,
                part.yScale, part.zScale, part.visible, part.skipDraw)).toList();
    }
    @Override public void close() {
        for (var state : saved) {
            state.part.loadPose(state.pose);
            state.part.xScale = state.sx; state.part.yScale = state.sy; state.part.zScale = state.sz;
            state.part.visible = state.visible; state.part.skipDraw = state.skipDraw;
        }
    }
}
