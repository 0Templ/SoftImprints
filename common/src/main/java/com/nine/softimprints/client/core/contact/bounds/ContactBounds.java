package com.nine.softimprints.client.core.contact.bounds;

public record ContactBounds(
        double minX,
        double minZ,
        double maxX,
        double maxZ
) {

    public ContactBounds intersection(double minX, double minZ, double maxX, double maxZ) {
        double nx0 = Math.max(this.minX, minX);
        double nz0 = Math.max(this.minZ, minZ);
        double nx1 = Math.min(this.maxX, maxX);
        double nz1 = Math.min(this.maxZ, maxZ);

        if (nx1 <= nx0 || nz1 <= nz0) {
            return null;
        }

        return new ContactBounds(nx0, nz0, nx1, nz1);
    }

    public boolean intersectsBlock(int blockX, int blockZ) {
        return maxX > blockX && minX < blockX + 1
                && maxZ > blockZ && minZ < blockZ + 1;
    }

    public boolean intersects(double minX, double minZ, double maxX, double maxZ) {
        return this.maxX > minX && this.minX < maxX
                && this.maxZ > minZ && this.minZ < maxZ;
    }

    public boolean containsPoint(double x, double z) {
        return x >= minX && x < maxX && z >= minZ && z < maxZ;
    }

}
