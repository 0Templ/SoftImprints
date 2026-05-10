package com.nine.softimprints.mixin.feature.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.client.core.contact.model.render.NoopVertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @Shadow
    protected EntityModel<? super LivingEntityRenderState> model;

    @Inject(
            method = ModelContactMixinTargets.LIVING_ENTITY_RENDERER_SUBMIT,
            at = @At(
                    value = "INVOKE",
                    target = ModelContactMixinTargets.SUBMIT_NODE_COLLECTOR_SUBMIT_MODEL,
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            ),
            require = 1,
            allow = 1
    )
    private void softimprints$beginLivingCapture(
            LivingEntityRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        LivingEntity entity = ModelContactSnapshotCache.resolveLivingEntity(renderState);
        if (entity == null) {
            return;
        }
        ModelContactSnapshotCache.beginLivingCapture(entity);
        if (!ModelContactSnapshotCache.isSessionActive()) {
            return;
        }
        this.model.setupAnim(renderState);
        this.model.renderToBuffer(
                poseStack,
                new NoopVertexConsumer(),
                0,
                0,
                -1
        );
    }

    @Inject(
            method = ModelContactMixinTargets.LIVING_ENTITY_RENDERER_SUBMIT,
            at = @At(
                    value = "INVOKE",
                    target = ModelContactMixinTargets.SUBMIT_NODE_COLLECTOR_SUBMIT_MODEL,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            require = 1,
            allow = 1
    )
    private void softimprints$finishLivingCapture(
            LivingEntityRenderState renderState,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState cameraRenderState,
            CallbackInfo ci
    ) {
        ModelContactSnapshotCache.finishLivingCapture();
    }
}
