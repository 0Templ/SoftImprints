package com.nine.softimprints.core.contact.model.capture;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.mixin.feature.model.ModelPartAccessor;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;

import java.util.List;
import java.util.Map;


public final class ModelPartObbCapturer {

    //
    private static final double PIXEL_SCALE = 1.0D / 16.0D;

    private ModelPartObbCapturer() {
    }

    public static void capture(ModelPart root, PoseStack poseStack, ModelContactCaptureSession session) {
        capturePart(root, poseStack, session);
    }

    private static void capturePart(ModelPart part, PoseStack poseStack, ModelContactCaptureSession session) {
        if (!part.visible) {
            return;
        }
        ModelPartAccessor access = (ModelPartAccessor) (Object) part;
        List<ModelPart.Cube> cubes = access.softimprints$cubes();
        Map<String, ModelPart> children = access.softimprints$children();
        if (cubes.isEmpty() && children.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        part.translateAndRotate(poseStack);

        if (!part.skipDraw) {
            Matrix4f pose = poseStack.last().pose();
            for (ModelPart.Cube cube : cubes) {
                captureCube(pose, cube, session);
            }
        }
        for (ModelPart child : children.values()) {
            capturePart(child, poseStack, session);
        }

        poseStack.popPose();
    }

    private static void captureCube(Matrix4f pose, ModelPart.Cube cube, ModelContactCaptureSession session) {
        double localX = (cube.minX + cube.maxX) * 0.5D * PIXEL_SCALE;
        double localY = (cube.minY + cube.maxY) * 0.5D * PIXEL_SCALE;
        double localZ = (cube.minZ + cube.maxZ) * 0.5D * PIXEL_SCALE;
        double halfX = (cube.maxX - cube.minX) * 0.5D * PIXEL_SCALE;
        double halfY = (cube.maxY - cube.minY) * 0.5D * PIXEL_SCALE;
        double halfZ = (cube.maxZ - cube.minZ) * 0.5D * PIXEL_SCALE;

        double centerX = pose.m00() * localX + pose.m10() * localY + pose.m20() * localZ + pose.m30();
        double centerY = pose.m01() * localX + pose.m11() * localY + pose.m21() * localZ + pose.m31();
        double centerZ = pose.m02() * localX + pose.m12() * localY + pose.m22() * localZ + pose.m32();

        session.captureObb(
                centerX, centerY, centerZ,
                pose.m00(), pose.m01(), pose.m02(),
                pose.m10(), pose.m11(), pose.m12(),
                pose.m20(), pose.m21(), pose.m22(),
                halfX, halfY, halfZ
        );
    }
}
