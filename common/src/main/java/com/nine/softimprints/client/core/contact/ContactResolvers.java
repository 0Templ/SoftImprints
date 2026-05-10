package com.nine.softimprints.client.core.contact;

import com.nine.softimprints.client.core.contact.bb.BoundingBoxContactResolver;
import com.nine.softimprints.client.core.contact.model.ModelContactResolver;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class ContactResolvers {

    public static final ContactResolver BB_RESOLVER = new BoundingBoxContactResolver();

    public static final ContactResolver MODEL_RESOLVER = new ModelContactResolver(BB_RESOLVER);

    public static ContactResolver getResolver(Entity entity){
        if (entity instanceof LivingEntity) {
            return MODEL_RESOLVER;
        }
        return BB_RESOLVER;
    }

}
