package com.nine.softimprints.client.core.contact.model.area;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.client.core.contact.bounds.ContactBounds;
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

    public static CompositeContactShape adapt(ModelContactSnapshot snapshot, Entity entity) {
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

        for (var box : contactBoxes) {
            double minX = box.minX() + originEntityX;
            double minZ = box.minZ() + originEntityZ;
            double maxX = box.maxX() + originEntityX;
            double maxZ = box.maxZ() + originEntityZ;

            minWorldX = Math.min(minWorldX, minX);
            maxWorldX = Math.max(maxWorldX, maxX);
            minWorldZ = Math.min(minWorldZ, minZ);
            maxWorldZ = Math.max(maxWorldZ, maxZ);
        }

        if (maxWorldX <= minWorldX || maxWorldZ <= minWorldZ) {
            return null;
        }

        int resolution = Constants.BASIC_RESOLUTION;
        double cellSize = 1.0D / resolution;

        int minCellX = (int) Math.floor(minWorldX * resolution);
        int maxCellX = (int) Math.ceil(maxWorldX * resolution);
        int minCellZ = (int) Math.floor(minWorldZ * resolution);
        int maxCellZ = (int) Math.ceil(maxWorldZ * resolution);

        int width = maxCellX - minCellX;
        int height = maxCellZ - minCellZ;
        if (width <= 0 || height <= 0) {
            return null;
        }

        double originX = minCellX * cellSize;
        double originZ = minCellZ * cellSize;

        int totalSamples = SAMPLE_SUBDIVISIONS * SAMPLE_SUBDIVISIONS;
        int minCoveredSamples = Math.max(1, (int) Math.ceil(totalSamples * COVERAGE_THRESHOLD));

        var builder = new CompositeContactShape.Builder();

        for (int z = 0; z < height; z++) {
            double cellWorldMinZ = originZ + z * cellSize;
            double cellWorldMaxZ = cellWorldMinZ + cellSize;
            double cellLocalMinZ = cellWorldMinZ - originEntityZ;
            double cellLocalMaxZ = cellWorldMaxZ - originEntityZ;
            int runStart = -1;

            for (int x = 0; x <= width; x++) {
                boolean filled = false;
                if (x < width) {
                    double cellWorldMinX = originX + x * cellSize;
                    double cellWorldMaxX = cellWorldMinX + cellSize;
                    double cellLocalMinX = cellWorldMinX - originEntityX;
                    double cellLocalMaxX = cellWorldMaxX - originEntityX;

                    filled = passesCoverageThreshold(
                            contactBoxes,
                            cellLocalMinX, cellLocalMaxX,
                            cellLocalMinZ, cellLocalMaxZ,
                            localBandMinY, localBandMaxY,
                            minCoveredSamples
                    );
                }

                if (filled && runStart < 0) {
                    runStart = x;
                }
                if ((!filled || x == width) && runStart >= 0) {
                    builder.add(new ContactBounds(
                            originX + runStart * cellSize,
                            cellWorldMinZ,
                            originX + x * cellSize,
                            cellWorldMaxZ
                    ));
                    runStart = -1;
                }
            }
        }

        if (builder.isEmpty()) {
            return null;
        }
        return builder.build(stampY);
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
