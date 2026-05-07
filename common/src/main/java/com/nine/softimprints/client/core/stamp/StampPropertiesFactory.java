package com.nine.softimprints.client.core.stamp;

import com.nine.softimprints.client.core.contact.ContactArea;
import net.minecraft.world.entity.Entity;

public class StampPropertiesFactory {

    public static int createSeed(Entity entity, ContactArea area, int entityId) {
        return StampSeedHelper.mixSeed(entityId,
                Double.hashCode(area.y),
                Double.hashCode(area.originX),
                Double.hashCode(area.originZ));
    }

    public static StampProperties create(Entity entity) {
        double vx = entity.getDeltaMovement().x;
        double vz = entity.getDeltaMovement().z;
        double speed = Math.sqrt(vx * vx + vz * vz);

        double t = Math.min(1.0, speed / 0.85);
        double scaleX = 1.0 - t;
        double scaleZ = 1.0;

        return new StampProperties(
                entity.yRotO, scaleX, scaleZ
        );
    }


}
