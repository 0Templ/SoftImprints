package com.nine.softimprints.core.contact.raster;

public record StampRaster(
    double originX,
    double originZ,
    double cellSize,
    int width,
    int height,
    byte[] mask
) {




}
