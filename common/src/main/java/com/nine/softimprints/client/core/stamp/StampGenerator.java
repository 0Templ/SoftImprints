package com.nine.softimprints.client.core.stamp;

import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.profile.ImprintProfile;

public final class StampGenerator {

    private static final int OUTER_JITTER_PADDING = 1;
    private static final int EROSION_SALT = 0x3C6EF35F;
    private static final int INNER_JITTER_SALT = 0xA54FF53B;
    private static final int OUTER_JITTER_SALT = 0x510E527F;

    private StampGenerator() {
    }

    public static StampMask generate(
            ImprintProfile profile,
            boolean[] contactMask, int size,
            int seed,
            ContactResult.StampStrategy stampStrategy,
            StampProperties properties
    ) {
        int padding = totalPadding(profile);
        int stampSize = size + padding * 2;

        byte[] values = new byte[stampSize * stampSize];

        boolean[] coverage = switch (stampStrategy) {
            case EXACT -> embedExact(
                    contactMask, size,
                    stampSize,
                    padding
            );
            case ELLIPSE -> embedEllipse(
                    contactMask, size,
                    stampSize,
                    padding,
                    properties.stretchX(), properties.stretchZ(),
                    properties.degree()
            );
        };

        var step = new LayerStep(
                coverage.clone(),
                coverage.clone(),
                new boolean[stampSize * stampSize]
        );

        applyLayers(values, step, coverage, profile, stampSize, seed);

        return new StampMask(values, stampSize, padding);
    }

    private static void applyMask(byte[] mask, boolean[] coverage, int size, byte value) {
        for (int i = 0; i < size * size; i++) {
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
            int stampSize,
            int seed
    ) {
        boolean firstProcessed = false;
        for (var layer : profile.getLayers()) {
            if (!layer.enable()) continue;
            boolean isFirst = !firstProcessed;
            firstProcessed = true;

            if (isFirst) {
                step = dilate(step, layer.expand(), stampSize);
                step = unionPaintWith(step, coverage, stampSize);
            } else {
                step = dilate(step, layer.expand(), stampSize);
            }
            step = applyErosion(step, layer.erosion(), stampSize, seed, layer.value());
            step = applyJitter(step, layer.innerJitter(), layer.outerJitter(), stampSize, seed, layer.value());
            applyMask(values, step.paint(), stampSize, layer.value());
        }
    }

    private static LayerStep unionPaintWith(LayerStep step, boolean[] coverage, int size) {
        boolean[] paint = step.paint().clone();
        for (int i = 0; i < size * size; i++) {
            if (coverage[i]) paint[i] = true;
        }
        return new LayerStep(step.body(), step.cleanBody(), paint);
    }

    private static LayerStep applyJitter(
            LayerStep step,
            float innerJitter,
            float outerJitter,
            int size,
            int seed,
            byte layerValue
    ) {
        if (outerJitter == 0 && innerJitter == 0) return step;

        boolean[] body = step.body().clone();
        boolean[] paint = step.paint().clone();
        int innerSeed = StampSeedHelper.mixSeed(seed, layerValue, INNER_JITTER_SALT);
        int outerSeed = StampSeedHelper.mixSeed(seed, layerValue, OUTER_JITTER_SALT);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = y * size + x;
                if (!step.paint()[i] && !step.body()[i] && hasNeighbor(step.paint(), size, x, y)) {
                    double outerNoise = StampSeedHelper.createNoise(y, x, outerSeed);
                    if (outerNoise < outerJitter) {
                        body[i] = true;
                        paint[i] = true;
                    }
                }
                if (step.body()[i] && !paint[i] && hasNeighbor(step.paint(), size, x, y)) {
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
            int size,
            int seed,
            byte layerValue
    ) {
        if (erosion == 0) return step;

        boolean[] paint = step.paint().clone();
        int erosionSeed = StampSeedHelper.mixSeed(seed, layerValue, EROSION_SALT);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int index = y * size + x;
                if (!paint[index]) continue;
                double exposure = edgeExposure(step.body(), size, x, y);
                if (exposure <= 0.0D) continue;

                double noise = StampSeedHelper.createNoise(x, y, erosionSeed);
                if (noise < erosion * exposure) {
                    paint[index] = false;
                }
            }
        }

        return new LayerStep(step.body(), step.cleanBody(), paint);
    }

    private static boolean hasNeighbor(boolean[] body, int size, int x, int y) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || nx >= size || ny < 0 || ny >= size) continue;
                if (body[ny * size + nx]) return true;
            }
        }
        return false;
    }

    private static double edgeExposure(boolean[] body, int size, int x, int y) {
        int openSides = 0;
        if (!isFilled(body, size, x - 1, y)) openSides++;
        if (!isFilled(body, size, x + 1, y)) openSides++;
        if (!isFilled(body, size, x, y - 1)) openSides++;
        if (!isFilled(body, size, x, y + 1)) openSides++;
        return openSides / 4.0D;
    }

    private static boolean isFilled(boolean[] body, int size, int x, int y) {
        return x >= 0 && x < size
                && y >= 0 && y < size
                && body[y * size + x];
    }

    private record LayerStep(boolean[] body, boolean[] cleanBody, boolean[] paint) {

    }

    // Todo: redo: custom rules, and smth else
    private static boolean[] embedExact(boolean[] mask, int size, int stampSize, int padding) {
        boolean[] ret = new boolean[stampSize * stampSize];
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (mask[y * size + x]) {
                    ret[(y + padding) * stampSize + (x + padding)] = true;
                }
            }
        }
        return ret;
    }

    private static boolean[] embedEllipse(
            boolean[] mask, int size, int stampSize, int padding,
            double scaleX, double scaleZ, double angleDeg
    ) {
        boolean[] ret = new boolean[stampSize * stampSize];
        double cx = (size - 1) / 2.0;
        double cz = (size - 1) / 2.0;

        double rad = Math.toRadians(angleDeg);
        double cos = Math.cos(rad);
        double sin = Math.sin(rad);

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (!mask[y * size + x]) continue;

                double dx = x - cx;
                double dz = y - cz;

                double lx = (dx * cos + dz * sin) / (cx * scaleX);
                double lz = (-dx * sin + dz * cos) / (cz * scaleZ);

                if (lx * lx + lz * lz > 1.0) continue;

                ret[(y + padding) * stampSize + (x + padding)] = true;
            }
        }
        return ret;
    }

    private static LayerStep dilate(LayerStep step, int expand, int size) {
        boolean[] paint = new boolean[size * size];
        if (expand <= 0) {
            return new LayerStep(step.body().clone(), step.cleanBody().clone(), paint);
        }

        boolean[] origClean = step.cleanBody();
        boolean[] origBody = step.body();
        boolean[] cleanBody = origClean.clone();
        boolean[] body = origBody.clone();

        boolean[] tmp = new boolean[size * size];
        for (int y = 0; y < size; y++) {
            int rowStart = y * size;
            for (int x = 0; x < size; x++) {
                if (!origClean[rowStart + x]) continue;
                int xMin = Math.max(0, x - expand);
                int xMax = Math.min(size - 1, x + expand);
                for (int nx = xMin; nx <= xMax; nx++) {
                    tmp[rowStart + nx] = true;
                }
            }
        }

        for (int y = 0; y < size; y++) {
            int rowStart = y * size;
            for (int x = 0; x < size; x++) {
                if (!tmp[rowStart + x]) continue;
                int yMin = Math.max(0, y - expand);
                int yMax = Math.min(size - 1, y + expand);
                for (int ny = yMin; ny <= yMax; ny++) {
                    int index = ny * size + x;
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
