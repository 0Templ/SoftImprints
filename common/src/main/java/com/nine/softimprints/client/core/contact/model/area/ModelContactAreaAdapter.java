package com.nine.softimprints.client.core.contact.model.area;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.contact.ContactArea;
import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.core.contact.model.ModelContactSupport;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshotBox;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class ModelContactAreaAdapter {

    private static final double COVERAGE_THRESHOLD = 0.35D;
    private static final int SAMPLE_SUBDIVISIONS = 3;

    private static final double BAND_FLOOR_SLOP = 0.125D;

    private static final double GROUND_RAYCAST_DEPTH = 1.5D;

    private ModelContactAreaAdapter() {
    }

    public static ContactResult adapt(ModelContactSnapshot snapshot, Entity entity) {
        if (snapshot == null || snapshot.isEmpty() || entity == null) {
            return null;
        }

        double originEntityX = entity.getX();
        double originEntityY = entity.getY();
        double originEntityZ = entity.getZ();

        double snapshotMinLocalY = snapshot.minY();
        double localBandMinY = snapshotMinLocalY - BAND_FLOOR_SLOP;
        double localBandMaxY = snapshotMinLocalY + ModelContactSupport.resolveBandHeight();

        List<ModelContactSnapshotBox> contactBoxes = collectContactBoxes(snapshot.boxes(), localBandMinY, localBandMaxY);
        if (contactBoxes.isEmpty()) {
            return null;
        }

        double stampY = resolveGroundY(entity, originEntityX, originEntityY, originEntityZ);

        double minWorldX = Double.POSITIVE_INFINITY;
        double maxWorldX = Double.NEGATIVE_INFINITY;
        double minWorldZ = Double.POSITIVE_INFINITY;
        double maxWorldZ = Double.NEGATIVE_INFINITY;

        for (ModelContactSnapshotBox box : contactBoxes) {
            minWorldX = Math.min(minWorldX, box.minX() + originEntityX);
            maxWorldX = Math.max(maxWorldX, box.maxX() + originEntityX);
            minWorldZ = Math.min(minWorldZ, box.minZ() + originEntityZ);
            maxWorldZ = Math.max(maxWorldZ, box.maxZ() + originEntityZ);
        }

        double spanX = maxWorldX - minWorldX;
        double spanZ = maxWorldZ - minWorldZ;
        double shortSpan = Math.min(spanX, spanZ);
        double longSpan = Math.max(spanX, spanZ);
        if (shortSpan <= 1.0E-6D) {
            return null;
        }

        int shortCells = Math.max(1, (int) Math.ceil(shortSpan * Constants.BASIC_RESOLUTION));
        double cellSize = shortSpan / shortCells;
        int size = Math.max(shortCells, (int) Math.ceil(longSpan / cellSize));

        double centerX = (minWorldX + maxWorldX) * 0.5D;
        double centerZ = (minWorldZ + maxWorldZ) * 0.5D;
        double half = size * cellSize * 0.5D;
        double originX = centerX - half;
        double originZ = centerZ - half;

        boolean[] bits = new boolean[size * size];
        boolean hasAny = false;

        int totalSamples = SAMPLE_SUBDIVISIONS * SAMPLE_SUBDIVISIONS;
        int minCoveredSamples = Math.max(1, (int) Math.ceil(totalSamples * COVERAGE_THRESHOLD));

        for (int z = 0; z < size; z++) {
            int row = z * size;
            double cellWorldMinZ = originZ + z * cellSize;
            double cellWorldMaxZ = cellWorldMinZ + cellSize;
            double cellLocalMinZ = cellWorldMinZ - originEntityZ;
            double cellLocalMaxZ = cellWorldMaxZ - originEntityZ;
            for (int x = 0; x < size; x++) {
                double cellWorldMinX = originX + x * cellSize;
                double cellWorldMaxX = cellWorldMinX + cellSize;
                double cellLocalMinX = cellWorldMinX - originEntityX;
                double cellLocalMaxX = cellWorldMaxX - originEntityX;
                if (passesCoverageThreshold(
                        contactBoxes,
                        cellLocalMinX, cellLocalMaxX,
                        cellLocalMinZ, cellLocalMaxZ,
                        localBandMinY, localBandMaxY,
                        minCoveredSamples
                )) {
                    bits[row + x] = true;
                    hasAny = true;
                }
            }
        }

        if (!hasAny) {
            return null;
        }

        return new ContactResult(
                ContactArea.create(originX, originZ, stampY, cellSize, size, bits),
                ContactResult.StampStrategy.EXACT
        );
    }

    private static double resolveGroundY(Entity entity, double x, double y, double z) {
        if (entity.onGround()) {
            return y;
        }
        Vec3 from = new Vec3(x, y, z);
        Vec3 to = new Vec3(x, y - GROUND_RAYCAST_DEPTH, z);
        HitResult hit = entity.level().clip(new ClipContext(
                from,
                to,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                entity
        ));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation().y;
        }
        return y;
    }

    private static List<ModelContactSnapshotBox> collectContactBoxes(List<ModelContactSnapshotBox> source, double bandMinY, double bandMaxY) {
        List<ModelContactSnapshotBox> result = new ArrayList<>(Math.min(24, source.size()));
        for (ModelContactSnapshotBox box : source) {
            if (box.maxY() < bandMinY || box.minY() > bandMaxY) {
                continue;
            }
            result.add(box);
        }
        return result;
    }

    private static boolean passesCoverageThreshold(
            List<ModelContactSnapshotBox> boxes,
            double minX,
            double maxX,
            double minZ,
            double maxZ,
            double minY,
            double maxY,
            int minCoveredSamples
    ) {
        if (!intersectsAny(boxes, minX, minY, minZ, maxX, maxY, maxZ)) {
            return false;
        }

        double sampleWidth = (maxX - minX) / SAMPLE_SUBDIVISIONS;
        double sampleDepth = (maxZ - minZ) / SAMPLE_SUBDIVISIONS;
        int totalSamples = SAMPLE_SUBDIVISIONS * SAMPLE_SUBDIVISIONS;
        int coveredSamples = 0;
        int processedSamples = 0;

        for (int sampleZ = 0; sampleZ < SAMPLE_SUBDIVISIONS; sampleZ++) {
            double sampleMinZ = minZ + sampleZ * sampleDepth;
            double sampleMaxZ = sampleMinZ + sampleDepth;
            for (int sampleX = 0; sampleX < SAMPLE_SUBDIVISIONS; sampleX++) {
                double sampleMinX = minX + sampleX * sampleWidth;
                double sampleMaxX = sampleMinX + sampleWidth;
                if (intersectsAny(boxes, sampleMinX, minY, sampleMinZ, sampleMaxX, maxY, sampleMaxZ)) {
                    coveredSamples++;
                    if (coveredSamples >= minCoveredSamples) {
                        return true;
                    }
                }

                processedSamples++;
                if (coveredSamples + (totalSamples - processedSamples) < minCoveredSamples) {
                    return false;
                }
            }
        }

        return false;
    }

    private static boolean intersectsAny(
            List<ModelContactSnapshotBox> boxes,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ
    ) {
        for (ModelContactSnapshotBox box : boxes) {
            if (box.intersectsAabb(minX, minY, minZ, maxX, maxY, maxZ)) {
                return true;
            }
        }
        return false;
    }
}
