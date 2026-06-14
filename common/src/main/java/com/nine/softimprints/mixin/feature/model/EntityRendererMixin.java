package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.core.contact.model.ModelContactRenderStateBridge;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class EntityRendererMixin {

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("HEAD"),
            require = 1,
            allow = 1
    )
    private void softimprints$invalidateStaleRenderState(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            float partialTicks,
            CallbackInfo ci
    ) {
        ModelContactRenderStateBridge bridge = (ModelContactRenderStateBridge) renderState;
        if (bridge.softimprints$getEntity() != null && bridge.softimprints$getEntity() != entity) {
            bridge.softimprints$setEntity(null);
        }
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V",
            at = @At("RETURN"),
            require = 1,
            allow = 1
    )
    private void softimprints$attachEntityToRenderState(
            LivingEntity entity,
            LivingEntityRenderState renderState,
            float partialTicks,
            CallbackInfo ci
    ) {
        ModelContactRenderStateBridge bridge = (ModelContactRenderStateBridge) renderState;
        bridge.softimprints$setEntity(entity);
    }
}
