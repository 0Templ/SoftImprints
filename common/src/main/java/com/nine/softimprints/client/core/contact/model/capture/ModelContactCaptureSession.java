package com.nine.softimprints.client.core.contact.model.capture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshotBox;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Pose;

import java.util.ArrayList;
import java.util.List;

public final class ModelContactCaptureSession {

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

    public void capture(PoseStack.Pose pose, List<ModelPart.Cube> cubes) {
        double horizontalScale = 1;
        for (ModelPart.Cube cube : cubes) {
            ModelContactSnapshotBox box = ModelContactSnapshotBox.fromCube(
                    pose,
                    cube,
                    horizontalScale,
                    1.0D,
                    this.cameraToEntityX,
                    this.cameraToEntityY,
                    this.cameraToEntityZ
            );
            if (box == null) {
                continue;
            }

            this.boxes.add(box);
            this.minX = Math.min(this.minX, box.minX());
            this.maxX = Math.max(this.maxX, box.maxX());
            this.minY = Math.min(this.minY, box.minY());
            this.maxY = Math.max(this.maxY, box.maxY());
            this.minZ = Math.min(this.minZ, box.minZ());
            this.maxZ = Math.max(this.maxZ, box.maxZ());
        }
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
