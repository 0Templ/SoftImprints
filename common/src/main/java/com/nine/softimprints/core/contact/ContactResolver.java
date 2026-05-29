package com.nine.softimprints.core.contact;

import net.minecraft.world.entity.Entity;

public interface ContactResolver {

   ContactResult resolve(Entity entity);

}
