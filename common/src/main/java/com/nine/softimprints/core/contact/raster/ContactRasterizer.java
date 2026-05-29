package com.nine.softimprints.core.contact.raster;

import com.nine.softimprints.core.contact.bounds.CompositeContactShape;
import com.nine.softimprints.core.contact.bounds.ContactBounds;
import net.minecraft.util.Mth;

public class ContactRasterizer {


    public static ContactRaster rasterize(CompositeContactShape shape, int size) {
        double cellSize = 1.0D / size;

        ContactBounds bounds = shape.bounds();

        int minCellX = Mth.floor(bounds.minX() * size);
        int maxCellX = Mth.ceil(bounds.maxX() * size);
        int minCellZ = Mth.floor(bounds.minZ() * size);
        int maxCellZ = Mth.ceil(bounds.maxZ() * size);

        int width = maxCellX - minCellX;
        int height = maxCellZ - minCellZ;

        if (width <= 0 || height <= 0) {
            return null;
        }

        double originX = minCellX / (double) size;
        double originZ = minCellZ / (double) size;

        boolean[] mask = new boolean[width * height];

        ContactBounds[] parts = shape.parts();
        boolean hasAny = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (ContactBounds part : parts) {
                    double cellMinX = originX + x * cellSize;
                    double cellMaxX = cellMinX + cellSize;
                    double cellMinZ = originZ + y * cellSize;
                    double cellMaxZ = cellMinZ + cellSize;
                    if (part.intersects(cellMinX, cellMinZ, cellMaxX, cellMaxZ)) {
                        mask[y * width + x] = true;
                        hasAny = true;
                        break;
                    }
                }
            }
        }

        if (!hasAny) {
            return null;
        }

        return new ContactRaster(originX, originZ, cellSize, width, height, mask);
    }


}
