package com.nine.softimprints.core.contact.bb;

import com.nine.softimprints.core.contact.ContactResolver;
import com.nine.softimprints.core.contact.ContactResult;
import com.nine.softimprints.core.contact.bounds.CompositeContactShape;
import net.minecraft.world.entity.Entity;

public class BoundingBoxContactResolver implements ContactResolver {

    public ContactResult resolve(Entity entity) {
        var bb = entity.getBoundingBox();

        double xW = bb.maxX - bb.minX;
        double zW = bb.maxZ - bb.minZ;

        if (xW <= 0.0D || zW <= 0.0D) {
            return null;
        }


        CompositeContactShape shape = CompositeContactShape.create(bb.minX, bb.minZ, bb.maxX, bb.maxZ, bb.minY);
        return new ContactResult(shape, ContactResult.StampStrategy.ELLIPSE);
    }

}
