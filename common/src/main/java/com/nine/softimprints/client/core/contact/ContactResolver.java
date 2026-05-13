package com.nine.softimprints.client.core.contact;

import com.nine.softimprints.client.core.contact.bounds.CompositeContactShape;
import net.minecraft.world.entity.Entity;

public interface ContactResolver {

   ContactResult resolve(Entity entity);

}
