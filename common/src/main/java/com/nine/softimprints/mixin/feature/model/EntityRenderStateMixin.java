package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.client.core.contact.model.ModelContactRenderStateBridge;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(EntityRenderState.class)
public abstract class EntityRenderStateMixin implements ModelContactRenderStateBridge {

    @Unique
    private Entity softimprints$entity;

    @Override
    public void softimprints$setEntity(Entity entity) {
        this.softimprints$entity = entity;
    }

    @Override
    public Entity softimprints$getEntity() {
        return this.softimprints$entity;
    }
}
