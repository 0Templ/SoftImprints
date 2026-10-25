package com.nine.softimprints.model.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.minecraft.core.Direction;

/** A horizontal rectangle with the source quad's vertex order and interpolation coordinates. */
record TopFaceClip(
        float originX, float originZ,
        float edgeAx, float edgeAz,
        float edgeBx, float edgeBz,
        float invDet,
        float minX, float maxX,
        float minZ, float maxZ
) {

    private static final float GEOMETRY_EPSILON = 1.0E-5F;
    private static final float AREA_EPSILON = 1.0E-7F;

    static TopFaceClip analyze(QuadView quad) {
        if (quad.lightFace() != Direction.UP) return null;
        float height = quad.y(0);
        for (int i = 1; i < 4; i++) {
            if (Math.abs(quad.y(i) - height) > GEOMETRY_EPSILON) return null;
        }

        float x0 = quad.x(0), z0 = quad.z(0);
        float edgeAx = quad.x(3) - x0, edgeAz = quad.z(3) - z0;
        float edgeBx = quad.x(1) - x0, edgeBz = quad.z(1) - z0;
        if (Math.abs(x0 + edgeAx + edgeBx - quad.x(2)) > GEOMETRY_EPSILON
                || Math.abs(z0 + edgeAz + edgeBz - quad.z(2)) > GEOMETRY_EPSILON
                || Math.min(Math.abs(edgeAx), Math.abs(edgeAz)) > GEOMETRY_EPSILON
                || Math.min(Math.abs(edgeBx), Math.abs(edgeBz)) > GEOMETRY_EPSILON) {
            return null;
        }

        float det = edgeAx * edgeBz - edgeBx * edgeAz;
        if (Math.abs(det) < AREA_EPSILON) return null;

        float minX = Math.min(Math.min(x0, quad.x(1)), Math.min(quad.x(2), quad.x(3)));
        float maxX = Math.max(Math.max(x0, quad.x(1)), Math.max(quad.x(2), quad.x(3)));
        float minZ = Math.min(Math.min(z0, quad.z(1)), Math.min(quad.z(2), quad.z(3)));
        float maxZ = Math.max(Math.max(z0, quad.z(1)), Math.max(quad.z(2), quad.z(3)));
        if (minX < -GEOMETRY_EPSILON || maxX > 1.0F + GEOMETRY_EPSILON
                || minZ < -GEOMETRY_EPSILON || maxZ > 1.0F + GEOMETRY_EPSILON) {
            return null;
        }
        return new TopFaceClip(x0, z0, edgeAx, edgeAz, edgeBx, edgeBz, 1.0F / det,
                minX, maxX, minZ, maxZ);
    }

    boolean crop(QuadView source, QuadEmitter output, float x0, float z0, float x1, float z1) {
        x0 = Math.max(x0, minX);
        x1 = Math.min(x1, maxX);
        z0 = Math.max(z0, minZ);
        z1 = Math.min(z1, maxZ);
        if (x1 - x0 <= AREA_EPSILON || z1 - z0 <= AREA_EPSILON) return false;

        output.copyFrom(source);
        for (int i = 0; i < 4; i++) {
            float x = source.x(i) < (minX + maxX) * 0.5F ? x0 : x1;
            float z = source.z(i) < (minZ + maxZ) * 0.5F ? z0 : z1;
            float a = clamp(((x - originX) * edgeBz - edgeBx * (z - originZ)) * invDet);
            float b = clamp((edgeAx * (z - originZ) - (x - originX) * edgeAz) * invDet);

            output.pos(i, x, source.y(i), z);
            output.uv(i, interpolate(source.u(0), source.u(1), source.u(2), source.u(3), a, b),
                    interpolate(source.v(0), source.v(1), source.v(2), source.v(3), a, b));
            output.color(i, interpolatePacked(source.color(0), source.color(1),
                    source.color(2), source.color(3), a, b, 8));
            output.lightmap(i, interpolatePacked(source.lightmap(0), source.lightmap(1),
                    source.lightmap(2), source.lightmap(3), a, b, 16));

            if (source.hasNormal(0) && source.hasNormal(1) && source.hasNormal(2) && source.hasNormal(3)) {
                float nx = interpolate(source.normalX(0), source.normalX(1), source.normalX(2), source.normalX(3), a, b);
                float ny = interpolate(source.normalY(0), source.normalY(1), source.normalY(2), source.normalY(3), a, b);
                float nz = interpolate(source.normalZ(0), source.normalZ(1), source.normalZ(2), source.normalZ(3), a, b);
                float length = (float) Math.sqrt(nx * nx + ny * ny + nz * nz);
                if (length > GEOMETRY_EPSILON) output.normal(i, nx / length, ny / length, nz / length);
            }
        }
        return true;
    }

    private static float clamp(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static float interpolate(float v0, float v1, float v2, float v3, float a, float b) {
        return (1.0F - a) * ((1.0F - b) * v0 + b * v1) + a * ((1.0F - b) * v3 + b * v2);
    }

    private static int interpolatePacked(int v0, int v1, int v2, int v3, float a, float b, int bits) {
        int result = 0;
        int mask = (1 << bits) - 1;
        for (int shift = 0; shift < 32; shift += bits) {
            int value = Math.round(interpolate((v0 >>> shift) & mask, (v1 >>> shift) & mask,
                    (v2 >>> shift) & mask, (v3 >>> shift) & mask, a, b));
            result |= value << shift;
        }
        return result;
    }
}
