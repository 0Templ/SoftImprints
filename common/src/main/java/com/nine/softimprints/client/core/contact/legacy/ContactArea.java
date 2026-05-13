package com.nine.softimprints.client.core.contact.legacy;

public class ContactArea {

    public final double originX, originZ, y;
    public final double cellSize;
    public final int size;
    public final boolean[] bits;

    private ContactArea(
            double originX, double originZ, double y,
            double cellSize,
            int size,
            boolean[] bits
    ){
        this.originX = originX;
        this.originZ = originZ;
        this.y = y;
        this.cellSize = cellSize;
        this.size = size;
        this.bits = bits;
    }

    public static ContactArea create(
            double originX, double originZ, double y,
            double cellSize,
            int size,
            boolean[] bits
    ) {
        if (bits.length != size * size) {
            throw new IllegalArgumentException("bits length must match mapSize * mapSize");
        }
        return new ContactArea(originX, originZ, y, cellSize, size, bits);
    }



}
