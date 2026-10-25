package com.nine.softimprints.mixin.feature.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherCaptureMixin {
    @WrapOperation(
            method = "submit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"),
            require = 1, allow = 1
    )
    private <S extends EntityRenderState> void softimprints$scopeContactOrigin(
            EntityRenderer<?, S> renderer, S state, PoseStack poses, SubmitNodeCollector collector,
            CameraRenderState camera, Operation<Void> original) {
        var entity = ModelContactSnapshotCache.resolveLivingEntity(state);
        if (entity == null) {
            original.call(renderer, state, poses, collector, camera);
            return;
        }

        ModelContactSnapshotCache.beginEntityRender(entity, poses);
        try { original.call(renderer, state, poses, collector, camera); }
        finally { ModelContactSnapshotCache.finishEntityRender(); }
    }
}
