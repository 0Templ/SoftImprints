/* todo: tests
package com.nine.softimprints.model.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MeshView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;

import java.util.ArrayList;
import java.util.List;

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

    static List<TopFaceClip> analyzeItAll(MeshView mesh) {
        List<TopFaceClip> clips = new ArrayList<>(mesh.size());
        boolean[] failed = {false};
        mesh.forEach(quad -> {
            if (failed[0]) {
                return;
            }
            TopFaceClip clip = analyze(quad);
            if (clip == null) {
                failed[0] = true;
            } else {
                clips.add(clip);
            }
        });
        return failed[0] ? null : clips;
    }

    static TopFaceClip analyze(QuadView quad) {
        float x0 = quad.x(0);
        float z0 = quad.z(0);
        float x1 = quad.x(1);
        float z1 = quad.z(1);
        float x2 = quad.x(2);
        float z2 = quad.z(2);
        float x3 = quad.x(3);
        float z3 = quad.z(3);

        float edgeBx = x1 - x0;
        float edgeBz = z1 - z0;
        float edgeAx = x3 - x0;
        float edgeAz = z3 - z0;

        if (Math.abs(x0 + edgeAx + edgeBx - x2) > GEOMETRY_EPSILON
                || Math.abs(z0 + edgeAz + edgeBz - z2) > GEOMETRY_EPSILON) {
            return null;
        }
        if (Math.min(Math.abs(edgeAx), Math.abs(edgeAz)) > GEOMETRY_EPSILON) {
            return null;
        }
        if (Math.min(Math.abs(edgeBx), Math.abs(edgeBz)) > GEOMETRY_EPSILON) {
            return null;
        }

        float det = edgeAx * edgeBz - edgeBx * edgeAz;
        if (Math.abs(det) < AREA_EPSILON) {
            return null;
        }

        float minX = Math.min(Math.min(x0, x1), Math.min(x2, x3));
        float maxX = Math.max(Math.max(x0, x1), Math.max(x2, x3));
        float minZ = Math.min(Math.min(z0, z1), Math.min(z2, z3));
        float maxZ = Math.max(Math.max(z0, z1), Math.max(z2, z3));

        return new TopFaceClip(
                x0, z0,
                edgeAx, edgeAz,
                edgeBx, edgeBz,
                1.0F / det,
                minX, maxX,
                minZ, maxZ
        );
    }

    float paramA(float x, float z) {
        return ((x - this.originX) * this.edgeBz - this.edgeBx * (z - this.originZ)) * this.invDet;
    }

    float paramB(float x, float z) {
        return (this.edgeAx * (z - this.originZ) - (x - this.originX) * this.edgeAz) * this.invDet;
    }
}
*/
