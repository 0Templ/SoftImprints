package com.nine.softimprints.client.core.stamp;

import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.core.contact.raster.ContactRaster;
import com.nine.softimprints.client.core.contact.raster.StampRaster;
import com.nine.softimprints.client.profile.ImprintProfile;

public final class StampGenerator {

    private static final int OUTER_JITTER_PADDING = 1;
    private static final int EROSION_SALT = 0x3C6EF35F;
    private static final int INNER_JITTER_SALT = 0xA54FF53B;
    private static final int OUTER_JITTER_SALT = 0x510E527F;

    private StampGenerator() {
    }

    public static StampRaster generate(
            ImprintProfile profile,
            ContactRaster raster,
            // Todo: move to params class
            int seed,
            ContactResult.StampStrategy stampStrategy,
            StampProperties properties
    ) {
        double cellSize = raster.cellSize();

        int padding = totalPadding(profile);

        double originX = raster.originX() - padding * cellSize;
        double originZ = raster.originZ() - padding * cellSize;

        int width = raster.width() + padding * 2;
        int height = raster.height() + padding * 2;

        boolean[] contactMask = raster.mask();
        byte[] values = new byte[width * height];

        boolean[] coverage = switch (stampStrategy) {
            case EXACT -> embedExact(
                    contactMask,
                    raster.width(), raster.height(),
                    width, height,
                    padding
            );
            case ELLIPSE -> embedEllipse(
                    contactMask,
                    raster.width(), raster.height(),
                    width, height,
                    padding,
                    properties.stretchX(), properties.stretchZ(),
                    properties.degree()
            );
        };

        var step = new LayerStep(
                coverage.clone(),
                coverage.clone(),
                new boolean[width * height]
        );

        applyLayers(values, step, coverage, profile, width, height, seed);

        return new StampRaster(
                originX, originZ,
                raster.cellSize(),
                width, height,
                values
        );
    }

    private static void applyMask(byte[] mask, boolean[] coverage, int width, int height, byte value) {
        for (int i = 0; i < width * height; i++) {
            if (!coverage[i]) continue;
            mask[i] = value;
        }
    }

    private static int totalPadding(ImprintProfile profile) {
        int sum = 0;
        for (var layer : profile.getLayers()) {
            if (!layer.enable()) continue;
            sum += layer.expand();
            sum += OUTER_JITTER_PADDING;
        }
        return sum;
    }

    private static void applyLayers(
            byte[] values,
            LayerStep step,
            boolean[] coverage,
            ImprintProfile profile,
            int width,
            int height,
            int seed
    ) {
        boolean firstProcessed = false;
        for (var layer : profile.getLayers()) {
            if (!layer.enable()) continue;
            boolean isFirst = !firstProcessed;
            firstProcessed = true;

            if (isFirst) {
                step = dilate(step, layer.expand(), width, height);
                step = unionPaintWith(step, coverage, width, height);
            } else {
                step = dilate(step, layer.expand(), width, height);
            }
            step = applyErosion(step, layer.erosion(), width, height, seed, layer.value());
            step = applyJitter(step, layer.innerJitter(), layer.outerJitter(), width, height, seed, layer.value());
            applyMask(values, step.paint(), width, height, layer.value());
        }
    }

    private static LayerStep unionPaintWith(LayerStep step, boolean[] coverage, int width, int height) {
        boolean[] paint = step.paint().clone();
        for (int i = 0; i < width * height; i++) {
            if (coverage[i]) paint[i] = true;
        }
        return new LayerStep(step.body(), step.cleanBody(), paint);
    }

    private static LayerStep applyJitter(
            LayerStep step,
            float innerJitter,
            float outerJitter,
            int width,
            int height,
            int seed,
            byte layerValue
    ) {
        if (outerJitter == 0 && innerJitter == 0) return step;

        boolean[] body = step.body().clone();
        boolean[] paint = step.paint().clone();
        int innerSeed = StampSeedHelper.mixSeed(seed, layerValue, INNER_JITTER_SALT);
        int outerSeed = StampSeedHelper.mixSeed(seed, layerValue, OUTER_JITTER_SALT);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = y * width + x;
                if (!step.paint()[i] && !step.body()[i] && hasNeighbor(step.paint(), width, height, x, y)) {
                    double outerNoise = StampSeedHelper.createNoise(y, x, outerSeed);
                    if (outerNoise < outerJitter) {
                        body[i] = true;
                        paint[i] = true;
                    }
                }
                if (step.body()[i] && !paint[i] && hasNeighbor(step.paint(), width, height, x, y)) {
                    double innerNoise = StampSeedHelper.createNoise(x, y, innerSeed);
                    if (innerNoise < innerJitter) {
                        paint[i] = true;
                    }
                }
            }
        }

        return new LayerStep(body, step.cleanBody(), paint);
    }

    private static LayerStep applyErosion(
            LayerStep step,
            float erosion,
            int width,
            int height,
            int seed,
            byte layerValue
    ) {
        if (erosion == 0) return step;

        boolean[] paint = step.paint().clone();
        int erosionSeed = StampSeedHelper.mixSeed(seed, layerValue, EROSION_SALT);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                if (!paint[index]) continue;
                double exposure = edgeExposure(step.body(), width, height, x, y);
                if (exposure <= 0.0D) continue;

                double noise = StampSeedHelper.createNoise(x, y, erosionSeed);
                if (noise < erosion * exposure) {
                    paint[index] = false;
                }
            }
        }

        return new LayerStep(step.body(), step.cleanBody(), paint);
    }

    private static boolean hasNeighbor(boolean[] body, int width, int height, int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || nx >= width || ny < 0 || ny >= height) continue;
                if (body[ny * width + nx]) return true;
            }
        }
        return false;
    }

    private static double edgeExposure(boolean[] body, int width, int height, int x, int y) {
        int openSides = 0;
        if (!isFilled(body, width, height, x - 1, y)) openSides++;
        if (!isFilled(body, width, height, x + 1, y)) openSides++;
        if (!isFilled(body, width, height, x, y - 1)) openSides++;
        if (!isFilled(body, width, height, x, y + 1)) openSides++;
        return openSides / 4.0D;
    }

    private static boolean isFilled(boolean[] body, int width, int height, int x, int y) {
        return x >= 0 && x < width
                && y >= 0 && y < height
                && body[y * width + x];
    }

    private record LayerStep(boolean[] body, boolean[] cleanBody, boolean[] paint) {

    }

    private static boolean[] embedExact(
            boolean[] mask,
            int oldW, int oldH,
            int w, int h,
            int padding
    ) {
        boolean[] ret = new boolean[w * h];
        for (int y = 0; y < oldH; y++) {
            for (int x = 0; x < oldW; x++) {
                if (mask[y * oldW + x]) {
                    ret[(y + padding) * w + (x + padding)] = true;
                }
            }
        }
        return ret;
    }

    private static boolean[] embedEllipse(
            boolean[] mask,
            int oldW, int oldH,
            int w, int h,
            int padding,
            double scaleX, double scaleZ, double angleDeg
    ) {
        boolean[] ret = new boolean[w * h];
        double cx = (oldW - 1) / 2.0D;
        double cz = (oldH - 1) / 2.0D;
        double rx = Math.max(0.5D, cx * scaleX);
        double rz = Math.max(0.5D, cz * scaleZ);

        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (int y = 0; y < oldH; y++) {
            for (int x = 0; x < oldW; x++) {
                if (!mask[y * oldW + x]) continue;

                double dx = x - cx;
                double dz = y - cz;

                double lx = (dx * cos + dz * sin) / rx;
                double lz = (-dx * sin + dz * cos) / rz;

                if (lx * lx + lz * lz > 1.0) continue;

                ret[(y + padding) * w + (x + padding)] = true;
            }
        }
        return ret;
    }

    private static LayerStep dilate(LayerStep step, int expand, int width, int height) {
        boolean[] paint = new boolean[width * height];
        if (expand <= 0) {
            return new LayerStep(step.body().clone(), step.cleanBody().clone(), paint);
        }

        boolean[] origClean = step.cleanBody();
        boolean[] origBody = step.body();
        boolean[] cleanBody = origClean.clone();
        boolean[] body = origBody.clone();

        boolean[] tmp = new boolean[width * height];
        for (int y = 0; y < height; y++) {
            int rowStart = y * width;
            for (int x = 0; x < width; x++) {
                if (!origClean[rowStart + x]) continue;
                int xMin = Math.max(0, x - expand);
                int xMax = Math.min(width - 1, x + expand);
                for (int nx = xMin; nx <= xMax; nx++) {
                    tmp[rowStart + nx] = true;
                }
            }
        }

        for (int y = 0; y < height; y++) {
            int rowStart = y * width;
            for (int x = 0; x < width; x++) {
                if (!tmp[rowStart + x]) continue;
                int yMin = Math.max(0, y - expand);
                int yMax = Math.min(height - 1, y + expand);
                for (int ny = yMin; ny <= yMax; ny++) {
                    int index = ny * width + x;
                    if (!origClean[index] && !origBody[index]) {
                        paint[index] = true;
                    }
                    cleanBody[index] = true;
                    body[index] = true;
                }
            }
        }
        return new LayerStep(body, cleanBody, paint);
    }




}
