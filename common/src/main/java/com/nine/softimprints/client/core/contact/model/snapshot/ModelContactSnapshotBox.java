package com.nine.softimprints.client.core.contact.model.snapshot;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Matrix4f;
import org.joml.Vector3f;

// TODO: reorganize??
public record ModelContactSnapshotBox(
        double centerX, double centerY, double centerZ,
        double axisXx, double axisXy, double axisXz,
        double axisYx, double axisYy, double axisYz,
        double axisZx, double axisZy, double axisZz,
        double extentX, double extentY, double extentZ,
        double minX, double maxX,
        double minY, double maxY,
        double minZ, double maxZ
) {

    private static final double EPSILON = 1.0E-7D;

    private static final double ORTHOGONALITY_TOLERANCE = 1.0E-3D;

    public static ModelContactSnapshotBox fromCube(
            PoseStack.Pose pose,
            ModelPart.Cube cube,
            double horizontalScale,
            double verticalScale,
            double offsetX,
            double offsetY,
            double offsetZ
    ) {
        double localMinX = cube.minX / 16.0D;
        double localMinY = cube.minY / 16.0D;
        double localMinZ = cube.minZ / 16.0D;
        double localMaxX = cube.maxX / 16.0D;
        double localMaxY = cube.maxY / 16.0D;
        double localMaxZ = cube.maxZ / 16.0D;

        double halfX = (localMaxX - localMinX) * 0.5D;
        double halfY = (localMaxY - localMinY) * 0.5D;
        double halfZ = (localMaxZ - localMinZ) * 0.5D;
        if (halfX <= EPSILON || halfY <= EPSILON || halfZ <= EPSILON) {
            return null;
        }

        Matrix4f matrix = pose.pose();
        Vector3f center = matrix.transformPosition(
                (float) ((localMinX + localMaxX) * 0.5D),
                (float) ((localMinY + localMaxY) * 0.5D),
                (float) ((localMinZ + localMaxZ) * 0.5D),
                new Vector3f()
        );
        Vector3f axisX = matrix.transformDirection(1.0F, 0.0F, 0.0F, new Vector3f());
        Vector3f axisY = matrix.transformDirection(0.0F, 1.0F, 0.0F, new Vector3f());
        Vector3f axisZ = matrix.transformDirection(0.0F, 0.0F, 1.0F, new Vector3f());

        double scaleX = axisX.length();
        double scaleY = axisY.length();
        double scaleZ = axisZ.length();
        if (scaleX <= EPSILON || scaleY <= EPSILON || scaleZ <= EPSILON) {
            return null;
        }

        axisX.div((float) scaleX);
        axisY.div((float) scaleY);
        axisZ.div((float) scaleZ);

        if (!axesAreOrthogonal(axisX, axisY, axisZ)) {
            return null;
        }

        double extentX = halfX * scaleX * horizontalScale;
        double extentY = halfY * scaleY * verticalScale;
        double extentZ = halfZ * scaleZ * horizontalScale;

        double radiusX = Math.abs(axisX.x()) * extentX + Math.abs(axisY.x()) * extentY + Math.abs(axisZ.x()) * extentZ;
        double radiusY = Math.abs(axisX.y()) * extentX + Math.abs(axisY.y()) * extentY + Math.abs(axisZ.y()) * extentZ;
        double radiusZ = Math.abs(axisX.z()) * extentX + Math.abs(axisY.z()) * extentY + Math.abs(axisZ.z()) * extentZ;

        double centerX = center.x() + offsetX;
        double centerY = center.y() + offsetY;
        double centerZ = center.z() + offsetZ;

        return new ModelContactSnapshotBox(
                centerX, centerY, centerZ,
                axisX.x(), axisX.y(), axisX.z(),
                axisY.x(), axisY.y(), axisY.z(),
                axisZ.x(), axisZ.y(), axisZ.z(),
                extentX, extentY, extentZ,
                centerX - radiusX, centerX + radiusX,
                centerY - radiusY, centerY + radiusY,
                centerZ - radiusZ, centerZ + radiusZ
        );
    }

    public boolean intersectsAabb(
            double boxMinX,
            double boxMinY,
            double boxMinZ,
            double boxMaxX,
            double boxMaxY,
            double boxMaxZ
    ) {
        if (this.maxX < boxMinX || this.minX > boxMaxX
                || this.maxY < boxMinY || this.minY > boxMaxY
                || this.maxZ < boxMinZ || this.minZ > boxMaxZ) {
            return false;
        }

        double aabbCenterX = (boxMinX + boxMaxX) * 0.5D;
        double aabbCenterY = (boxMinY + boxMaxY) * 0.5D;
        double aabbCenterZ = (boxMinZ + boxMaxZ) * 0.5D;

        double ax = (boxMaxX - boxMinX) * 0.5D;
        double ay = (boxMaxY - boxMinY) * 0.5D;
        double az = (boxMaxZ - boxMinZ) * 0.5D;
        double bx = this.extentX;
        double by = this.extentY;
        double bz = this.extentZ;

        double tx = this.centerX - aabbCenterX;
        double ty = this.centerY - aabbCenterY;
        double tz = this.centerZ - aabbCenterZ;

        double r00 = this.axisXx;
        double r01 = this.axisYx;
        double r02 = this.axisZx;
        double r10 = this.axisXy;
        double r11 = this.axisYy;
        double r12 = this.axisZy;
        double r20 = this.axisXz;
        double r21 = this.axisYz;
        double r22 = this.axisZz;

        double ar00 = Math.abs(r00) + EPSILON;
        double ar01 = Math.abs(r01) + EPSILON;
        double ar02 = Math.abs(r02) + EPSILON;
        double ar10 = Math.abs(r10) + EPSILON;
        double ar11 = Math.abs(r11) + EPSILON;
        double ar12 = Math.abs(r12) + EPSILON;
        double ar20 = Math.abs(r20) + EPSILON;
        double ar21 = Math.abs(r21) + EPSILON;
        double ar22 = Math.abs(r22) + EPSILON;

        if (Math.abs(tx) > ax + bx * ar00 + by * ar01 + bz * ar02) {
            return false;
        }
        if (Math.abs(ty) > ay + bx * ar10 + by * ar11 + bz * ar12) {
            return false;
        }
        if (Math.abs(tz) > az + bx * ar20 + by * ar21 + bz * ar22) {
            return false;
        }

        double tOnBx = tx * this.axisXx + ty * this.axisXy + tz * this.axisXz;
        double tOnBy = tx * this.axisYx + ty * this.axisYy + tz * this.axisYz;
        double tOnBz = tx * this.axisZx + ty * this.axisZy + tz * this.axisZz;
        if (Math.abs(tOnBx) > bx + ax * ar00 + ay * ar10 + az * ar20) {
            return false;
        }
        if (Math.abs(tOnBy) > by + ax * ar01 + ay * ar11 + az * ar21) {
            return false;
        }
        if (Math.abs(tOnBz) > bz + ax * ar02 + ay * ar12 + az * ar22) {
            return false;
        }

        if (Math.abs(tz * r10 - ty * r20) > ay * ar20 + az * ar10 + by * ar02 + bz * ar01) {
            return false;
        }
        if (Math.abs(tz * r11 - ty * r21) > ay * ar21 + az * ar11 + bx * ar02 + bz * ar00) {
            return false;
        }
        if (Math.abs(tz * r12 - ty * r22) > ay * ar22 + az * ar12 + bx * ar01 + by * ar00) {
            return false;
        }

        if (Math.abs(tx * r20 - tz * r00) > ax * ar20 + az * ar00 + by * ar12 + bz * ar11) {
            return false;
        }
        if (Math.abs(tx * r21 - tz * r01) > ax * ar21 + az * ar01 + bx * ar12 + bz * ar10) {
            return false;
        }
        if (Math.abs(tx * r22 - tz * r02) > ax * ar22 + az * ar02 + bx * ar11 + by * ar10) {
            return false;
        }

        if (Math.abs(ty * r00 - tx * r10) > ax * ar10 + ay * ar00 + by * ar22 + bz * ar21) {
            return false;
        }
        if (Math.abs(ty * r01 - tx * r11) > ax * ar11 + ay * ar01 + bx * ar22 + bz * ar20) {
            return false;
        }
        return Math.abs(ty * r02 - tx * r12) <= ax * ar12 + ay * ar02 + bx * ar21 + by * ar20;
    }

    private static boolean axesAreOrthogonal(Vector3f axisX, Vector3f axisY, Vector3f axisZ) {
        double dotXY = (double) axisX.x() * axisY.x()
                + (double) axisX.y() * axisY.y()
                + (double) axisX.z() * axisY.z();
        if (Math.abs(dotXY) > ORTHOGONALITY_TOLERANCE) {
            return false;
        }
        double dotYZ = (double) axisY.x() * axisZ.x()
                + (double) axisY.y() * axisZ.y()
                + (double) axisY.z() * axisZ.z();
        if (Math.abs(dotYZ) > ORTHOGONALITY_TOLERANCE) {
            return false;
        }
        double dotXZ = (double) axisX.x() * axisZ.x()
                + (double) axisX.y() * axisZ.y()
                + (double) axisX.z() * axisZ.z();
        return Math.abs(dotXZ) <= ORTHOGONALITY_TOLERANCE;
    }
}
