package com.nine.softimprints.client.core.contact.bb;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.contact.ContactArea;
import com.nine.softimprints.client.core.contact.ContactResolver;
import com.nine.softimprints.client.core.contact.ContactResult;
import net.minecraft.world.entity.Entity;

public class BoundingBoxContactResolver implements ContactResolver {

    public ContactResult resolve(Entity entity){
        var bb = entity.getBoundingBox();

        double xW = bb.maxX - bb.minX;
        double zW = bb.maxZ - bb.minZ;

        if (xW <= 0.0D || zW <= 0.0D) {
            return null;
        }

        double maxDiff = Math.max(xW, zW);

        int size = (int) Math.ceil(maxDiff * Constants.BASIC_RESOLUTION);
        double cellSize = (double) 1 / 16;
        boolean[] bits = new boolean[size * size];

        int cellsX = Math.max(1, Math.min(size, (int) Math.ceil(xW / maxDiff * size)));
        int cellsZ = Math.max(1, Math.min(size, (int) Math.ceil(zW / maxDiff * size)));

        for (int gz = 0; gz < size; gz++) {
            for (int gx = 0; gx < size; gx++) {
                bits[gz * size + gx] = gx < cellsX && gz < cellsZ;
            }
        }

        return new ContactResult(ContactArea.create(
                bb.minX, bb.minZ, bb.minY,
                cellSize, size, bits),
                ContactResult.StampStrategy.ELLIPSE);
    }

}
