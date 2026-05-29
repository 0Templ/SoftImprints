package com.nine.softimprints.core.contact.model.capture;

import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshotBox;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.List;

public final class ModelContactCaptureSession {

    private static final int MAX_CAPTURED_BOXES = 4096;

    private final int entityId;
    private final long gameTime;
    private final long frameIndex;
    private final Pose capturePose;

    private final float captureBodyYaw;

    private final double cameraToEntityX;
    private final double cameraToEntityY;
    private final double cameraToEntityZ;
    private final List<ModelContactSnapshotBox> boxes = new ArrayList<>();

    private double minX = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;
    private double minZ = Double.POSITIVE_INFINITY;
    private double maxZ = Double.NEGATIVE_INFINITY;

    public ModelContactCaptureSession(
            int entityId,
            long gameTime,
            long frameIndex,
            Pose capturePose,
            float captureBodyYaw,
            double entityX,
            double entityY,
            double entityZ,
            double cameraX,
            double cameraY,
            double cameraZ
    ) {
        this.entityId = entityId;
        this.gameTime = gameTime;
        this.frameIndex = frameIndex;
        this.capturePose = capturePose;
        this.captureBodyYaw = captureBodyYaw;

        this.cameraToEntityX = cameraX - entityX;
        this.cameraToEntityY = cameraY - entityY;
        this.cameraToEntityZ = cameraZ - entityZ;
    }

    public void captureMeshQuad(
            double x0, double y0, double z0,
            double x1, double y1, double z1,
            double x2, double y2, double z2,
            double x3, double y3, double z3
    ) {
        if (this.boxes.size() >= MAX_CAPTURED_BOXES) {
            return;
        }

        ModelContactSnapshotBox box = ModelContactSnapshotBox.fromQuad(
                x0 + this.cameraToEntityX,
                y0 + this.cameraToEntityY,
                z0 + this.cameraToEntityZ,
                x1 + this.cameraToEntityX,
                y1 + this.cameraToEntityY,
                z1 + this.cameraToEntityZ,
                x2 + this.cameraToEntityX,
                y2 + this.cameraToEntityY,
                z2 + this.cameraToEntityZ,
                x3 + this.cameraToEntityX,
                y3 + this.cameraToEntityY,
                z3 + this.cameraToEntityZ
        );
        if (box != null) {
            addBox(box);
        }
    }

    private void addBox(ModelContactSnapshotBox box) {
        if (this.boxes.size() >= MAX_CAPTURED_BOXES) {
            return;
        }
        this.boxes.add(box);
        this.minX = Math.min(this.minX, box.minX());
        this.maxX = Math.max(this.maxX, box.maxX());
        this.minY = Math.min(this.minY, box.minY());
        this.maxY = Math.max(this.maxY, box.maxY());
        this.minZ = Math.min(this.minZ, box.minZ());
        this.maxZ = Math.max(this.maxZ, box.maxZ());
    }

    public ModelContactSnapshot build() {
        if (this.boxes.isEmpty()) {
            return null;
        }

        return new ModelContactSnapshot(
                this.entityId,
                this.frameIndex,
                this.gameTime,
                this.capturePose,
                this.captureBodyYaw,
                this.minX,
                this.maxX,
                this.minY,
                this.maxY,
                this.minZ,
                this.maxZ,
                this.boxes
        );
    }
}
