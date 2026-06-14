package com.nine.softimprints.core.contact.model.snapshot;

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

    private static final double MIN_MESH_EXTENT = 1.0E-4D;

    public static ModelContactSnapshotBox fromObb(
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
        if (!allFinite(
                centerX, centerY, centerZ,
                colXx, colXy, colXz,
                colYx, colYy, colYz,
                colZx, colZy, colZz,
                halfX, halfY, halfZ
        )) {
            return null;
        }

        double lenX = length(colXx, colXy, colXz);
        double lenY = length(colYx, colYy, colYz);
        double lenZ = length(colZx, colZy, colZz);
        if (lenX <= EPSILON || lenY <= EPSILON || lenZ <= EPSILON) {
            return null;
        }

        double extentX = halfX * lenX;
        double extentY = halfY * lenY;
        double extentZ = halfZ * lenZ;
        if (extentX <= MIN_MESH_EXTENT && extentY <= MIN_MESH_EXTENT && extentZ <= MIN_MESH_EXTENT) {
            return null;
        }
        extentX = Math.max(extentX, MIN_MESH_EXTENT);
        extentY = Math.max(extentY, MIN_MESH_EXTENT);
        extentZ = Math.max(extentZ, MIN_MESH_EXTENT);

        double axisXx = colXx / lenX;
        double axisXy = colXy / lenX;
        double axisXz = colXz / lenX;
        double axisYx = colYx / lenY;
        double axisYy = colYy / lenY;
        double axisYz = colYz / lenY;
        double axisZx = colZx / lenZ;
        double axisZy = colZy / lenZ;
        double axisZz = colZz / lenZ;

        double radiusX = Math.abs(axisXx) * extentX + Math.abs(axisYx) * extentY + Math.abs(axisZx) * extentZ;
        double radiusY = Math.abs(axisXy) * extentX + Math.abs(axisYy) * extentY + Math.abs(axisZy) * extentZ;
        double radiusZ = Math.abs(axisXz) * extentX + Math.abs(axisYz) * extentY + Math.abs(axisZz) * extentZ;

        return new ModelContactSnapshotBox(
                centerX, centerY, centerZ,
                axisXx, axisXy, axisXz,
                axisYx, axisYy, axisYz,
                axisZx, axisZy, axisZz,
                extentX, extentY, extentZ,
                centerX - radiusX, centerX + radiusX,
                centerY - radiusY, centerY + radiusY,
                centerZ - radiusZ, centerZ + radiusZ
        );
    }

    public static ModelContactSnapshotBox fromQuad(
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
        if (!allFinite(
                x0, y0, z0,
                x1, y1, z1,
                x2, y2, z2,
                x3, y3, z3
        )) {
            return null;
        }

        double axisXx = x1 - x0;
        double axisXy = y1 - y0;
        double axisXz = z1 - z0;
        double axisXLen = length(axisXx, axisXy, axisXz);
        if (axisXLen <= EPSILON) {
            return null;
        }
        axisXx /= axisXLen;
        axisXy /= axisXLen;
        axisXz /= axisXLen;

        double edgeVx = x3 - x0;
        double edgeVy = y3 - y0;
        double edgeVz = z3 - z0;
        double normalX = axisXy * edgeVz - axisXz * edgeVy;
        double normalY = axisXz * edgeVx - axisXx * edgeVz;
        double normalZ = axisXx * edgeVy - axisXy * edgeVx;
        double normalLen = length(normalX, normalY, normalZ);
        if (normalLen <= EPSILON) {
            edgeVx = x2 - x0;
            edgeVy = y2 - y0;
            edgeVz = z2 - z0;
            normalX = axisXy * edgeVz - axisXz * edgeVy;
            normalY = axisXz * edgeVx - axisXx * edgeVz;
            normalZ = axisXx * edgeVy - axisXy * edgeVx;
            normalLen = length(normalX, normalY, normalZ);
            if (normalLen <= EPSILON) {
                return null;
            }
        }
        normalX /= normalLen;
        normalY /= normalLen;
        normalZ /= normalLen;

        double axisZx = normalY * axisXz - normalZ * axisXy;
        double axisZy = normalZ * axisXx - normalX * axisXz;
        double axisZz = normalX * axisXy - normalY * axisXx;
        double axisZLen = length(axisZx, axisZy, axisZz);
        if (axisZLen <= EPSILON) {
            return null;
        }
        axisZx /= axisZLen;
        axisZy /= axisZLen;
        axisZz /= axisZLen;

        double p0x = dot(x0, y0, z0, axisXx, axisXy, axisXz);
        double p1x = dot(x1, y1, z1, axisXx, axisXy, axisXz);
        double p2x = dot(x2, y2, z2, axisXx, axisXy, axisXz);
        double p3x = dot(x3, y3, z3, axisXx, axisXy, axisXz);

        double p0y = dot(x0, y0, z0, normalX, normalY, normalZ);
        double p1y = dot(x1, y1, z1, normalX, normalY, normalZ);
        double p2y = dot(x2, y2, z2, normalX, normalY, normalZ);
        double p3y = dot(x3, y3, z3, normalX, normalY, normalZ);

        double p0z = dot(x0, y0, z0, axisZx, axisZy, axisZz);
        double p1z = dot(x1, y1, z1, axisZx, axisZy, axisZz);
        double p2z = dot(x2, y2, z2, axisZx, axisZy, axisZz);
        double p3z = dot(x3, y3, z3, axisZx, axisZy, axisZz);

        double minX = min4(p0x, p1x, p2x, p3x);
        double maxX = max4(p0x, p1x, p2x, p3x);
        double minY = min4(p0y, p1y, p2y, p3y);
        double maxY = max4(p0y, p1y, p2y, p3y);
        double minZ = min4(p0z, p1z, p2z, p3z);
        double maxZ = max4(p0z, p1z, p2z, p3z);

        double extentX = Math.max((maxX - minX) * 0.5D, MIN_MESH_EXTENT);
        double extentY = Math.max((maxY - minY) * 0.5D, MIN_MESH_EXTENT);
        double extentZ = Math.max((maxZ - minZ) * 0.5D, MIN_MESH_EXTENT);
        if (extentX <= MIN_MESH_EXTENT && extentZ <= MIN_MESH_EXTENT) {
            return null;
        }

        double centerOnX = (minX + maxX) * 0.5D;
        double centerOnY = (minY + maxY) * 0.5D;
        double centerOnZ = (minZ + maxZ) * 0.5D;

        double centerX = axisXx * centerOnX + normalX * centerOnY + axisZx * centerOnZ;
        double centerY = axisXy * centerOnX + normalY * centerOnY + axisZy * centerOnZ;
        double centerZ = axisXz * centerOnX + normalZ * centerOnY + axisZz * centerOnZ;

        double radiusX = Math.abs(axisXx) * extentX + Math.abs(normalX) * extentY + Math.abs(axisZx) * extentZ;
        double radiusY = Math.abs(axisXy) * extentX + Math.abs(normalY) * extentY + Math.abs(axisZy) * extentZ;
        double radiusZ = Math.abs(axisXz) * extentX + Math.abs(normalZ) * extentY + Math.abs(axisZz) * extentZ;

        return new ModelContactSnapshotBox(
                centerX, centerY, centerZ,
                axisXx, axisXy, axisXz,
                normalX, normalY, normalZ,
                axisZx, axisZy, axisZz,
                extentX, extentY, extentZ,
                centerX - radiusX, centerX + radiusX,
                centerY - radiusY, centerY + radiusY,
                centerZ - radiusZ, centerZ + radiusZ
        );
    }

    private static boolean allFinite(double... values) {
        for (double value : values) {
            if (!Double.isFinite(value)) {
                return false;
            }
        }
        return true;
    }

    private static double length(
            double x,
            double y,
            double z
    ) {
        return Math.sqrt(x * x + y * y + z * z);
    }

    private static double dot(
            double ax,
            double ay,
            double az,
            double bx,
            double by,
            double bz
    ) {
        return ax * bx + ay * by + az * bz;
    }

    private static double min4(
            double a,
            double b,
            double c,
            double d
    ) {
        return Math.min(Math.min(a, b), Math.min(c, d));
    }

    private static double max4(
            double a,
            double b,
            double c,
            double d
    ) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    private static double rotateX(
            double x,
            double z,
            double cos,
            double sin
    ) {
        return x * cos + z * sin;
    }

    private static double rotateZ(
            double x,
            double z,
            double cos,
            double sin
    ) {
        return z * cos - x * sin;
    }

    public ModelContactSnapshotBox rotateAroundY(double radians) {
        if (Math.abs(radians) <= EPSILON) {
            return this;
        }
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);

        double rotatedCenterX = rotateX(this.centerX, this.centerZ, cos, sin);
        double rotatedCenterZ = rotateZ(this.centerX, this.centerZ, cos, sin);

        double rotatedAxisXx = rotateX(this.axisXx, this.axisXz, cos, sin);
        double rotatedAxisXz = rotateZ(this.axisXx, this.axisXz, cos, sin);
        double rotatedAxisYx = rotateX(this.axisYx, this.axisYz, cos, sin);
        double rotatedAxisYz = rotateZ(this.axisYx, this.axisYz, cos, sin);
        double rotatedAxisZx = rotateX(this.axisZx, this.axisZz, cos, sin);
        double rotatedAxisZz = rotateZ(this.axisZx, this.axisZz, cos, sin);

        double radiusX = Math.abs(rotatedAxisXx) * this.extentX
                + Math.abs(rotatedAxisYx) * this.extentY
                + Math.abs(rotatedAxisZx) * this.extentZ;
        double radiusY = Math.abs(this.axisXy) * this.extentX
                + Math.abs(this.axisYy) * this.extentY
                + Math.abs(this.axisZy) * this.extentZ;
        double radiusZ = Math.abs(rotatedAxisXz) * this.extentX
                + Math.abs(rotatedAxisYz) * this.extentY
                + Math.abs(rotatedAxisZz) * this.extentZ;

        return new ModelContactSnapshotBox(
                rotatedCenterX, this.centerY, rotatedCenterZ,
                rotatedAxisXx, this.axisXy, rotatedAxisXz,
                rotatedAxisYx, this.axisYy, rotatedAxisYz,
                rotatedAxisZx, this.axisZy, rotatedAxisZz,
                this.extentX, this.extentY, this.extentZ,
                rotatedCenterX - radiusX, rotatedCenterX + radiusX,
                this.centerY - radiusY, this.centerY + radiusY,
                rotatedCenterZ - radiusZ, rotatedCenterZ + radiusZ
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

}
