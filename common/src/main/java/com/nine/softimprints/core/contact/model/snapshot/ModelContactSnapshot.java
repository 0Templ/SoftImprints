package com.nine.softimprints.core.contact.model.snapshot;

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
        List<ModelContactSnapshotBox> boxes,
        double captureWidth, double captureHeight
) {
    public ModelContactSnapshot(int entityId, long frameIndex, long gameTime, Pose pose, float yaw,
                                double minX, double maxX, double minY, double maxY, double minZ, double maxZ,
                                List<ModelContactSnapshotBox> boxes) {
        this(entityId, frameIndex, gameTime, pose, yaw, minX, maxX, minY, maxY, minZ, maxZ, boxes, Double.NaN, Double.NaN);
    }

    public ModelContactSnapshot {
        boxes = List.copyOf(boxes);
    }

    public boolean isEmpty() {
        return this.boxes.isEmpty();
    }

    public boolean matchesDimensions(double width, double height) {
        return (Double.isNaN(captureWidth) || Math.abs(width - captureWidth) <= 1.0E-5)
                && (Double.isNaN(captureHeight) || Math.abs(height - captureHeight) <= 1.0E-5);
    }
}
