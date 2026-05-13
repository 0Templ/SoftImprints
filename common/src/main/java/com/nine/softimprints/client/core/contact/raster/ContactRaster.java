package com.nine.softimprints.client.core.contact.raster;

public record ContactRaster(
    double originX,
    double originZ,
    double cellSize,
    int width,
    int height,
    boolean[] mask
) {




}
