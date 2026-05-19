package com.nine.softimprints.client.core.contact.bounds;


import java.util.ArrayList;
import java.util.List;

public record CompositeContactShape(ContactBounds bounds, ContactBounds[] parts, double y) {

    public static CompositeContactShape create(
            double minX,
            double minZ,
            double maxX,
            double maxZ,
            double y
    ) {
        var bounds = new ContactBounds(minX, minZ, maxX, maxZ);
        return new CompositeContactShape(
                bounds,
                new ContactBounds[]{bounds},
                y
        );
    }

    public boolean intersectsBlock(
            double minX,
            double minZ,
            double maxX,
            double maxZ
    ) {
        for (ContactBounds part : parts) {
            if (part.intersects(minX, minZ, maxX, maxZ)) {
                return true;
            }
        }
        return false;
    }

    public static final class Builder {

        private final List<ContactBounds> parts = new ArrayList<>();

        private double minX = Double.POSITIVE_INFINITY;
        private double minZ = Double.POSITIVE_INFINITY;
        private double maxX = Double.NEGATIVE_INFINITY;
        private double maxZ = Double.NEGATIVE_INFINITY;

        public void add(ContactBounds part) {
            parts.add(part);

            minX = Math.min(minX, part.minX());
            minZ = Math.min(minZ, part.minZ());
            maxX = Math.max(maxX, part.maxX());
            maxZ = Math.max(maxZ, part.maxZ());
        }

        public boolean isEmpty() {
            return parts.isEmpty();
        }

        public CompositeContactShape build(double y) {
            return new CompositeContactShape(
                    new ContactBounds(minX, minZ, maxX, maxZ),
                    parts.toArray(ContactBounds[]::new),
                    y
            );
        }
    }


}
