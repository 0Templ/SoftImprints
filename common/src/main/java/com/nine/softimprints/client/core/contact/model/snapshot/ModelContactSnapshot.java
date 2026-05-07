package com.nine.softimprints.client.core.contact.model.snapshot;

import net.minecraft.world.entity.Pose;

import java.util.List;

public record ModelContactSnapshot(
        int entityId,
        long frameIndex,
        long gameTime,
        Pose capturePose,
        float captureBodyYaw,
        double minX,
        double maxX,
        double minY,
        double maxY,
        double minZ,
        double maxZ,
        List<ModelContactSnapshotBox> boxes
) {

    public ModelContactSnapshot {
        boxes = List.copyOf(boxes);
    }

    public boolean isEmpty() {
        return this.boxes.isEmpty();
    }
}
