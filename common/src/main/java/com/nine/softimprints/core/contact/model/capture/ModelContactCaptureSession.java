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
            float captureBodyYaw
    ) {
        this.entityId = entityId;
        this.gameTime = gameTime;
        this.frameIndex = frameIndex;
        this.capturePose = capturePose;
        this.captureBodyYaw = captureBodyYaw;
    }

    public float captureBodyYaw() {
        return this.captureBodyYaw;
    }

    public void captureMeshQuad(
            double x0,
            double y0,
            double z0,
            double x1,
            double y1,
            double z1,
            double x2,
            double y2,
            double z2,
            double x3,
            double y3,
            double z3
    ) {
        if (this.boxes.size() >= MAX_CAPTURED_BOXES) {
            return;
        }

        ModelContactSnapshotBox box = ModelContactSnapshotBox.fromQuad(
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3
        );
        if (box != null) {
            addBox(box);
        }
    }

    public void captureObb(
            double centerX,
            double centerY,
            double centerZ,
            double colXx,
            double colXy,
            double colXz,
            double colYx,
            double colYy,
            double colYz,
            double colZx,
            double colZy,
            double colZz,
            double halfX,
            double halfY,
            double halfZ
    ) {
        if (this.boxes.size() >= MAX_CAPTURED_BOXES) {
            return;
        }

        ModelContactSnapshotBox box = ModelContactSnapshotBox.fromObb(
                centerX, centerY, centerZ,
                colXx, colXy, colXz,
                colYx, colYy, colYz,
                colZx, colZy, colZz,
                halfX, halfY, halfZ
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
