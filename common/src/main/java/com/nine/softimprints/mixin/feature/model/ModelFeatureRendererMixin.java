package com.nine.softimprints.mixin.feature.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ModelFeatureRenderer.class)
public abstract class ModelFeatureRendererMixin {

    @WrapOperation(
            method = ModelContactMixinTargets.MODEL_FEATURE_RENDER_MODEL,
            at = @At(
                    value = "INVOKE",
                    target = ModelContactMixinTargets.MODEL_RENDER_TO_BUFFER,
                    ordinal = 0
            ),
            require = 1,
            allow = 1
    )
    private void softimprints$captureBaseModelFromRealRender(
            Model<?> model,
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color,
            Operation<Void> original,
            SubmitNodeStorage.ModelSubmit<?> modelSubmit,
            RenderType renderType,
            VertexConsumer originalBuffer,
            OutlineBufferSource outlineBufferSource,
            MultiBufferSource.BufferSource crumblingBufferSource
    ) {
        boolean capturing = ModelContactSnapshotCache.tryBeginSubmittedBaseModelCapture(modelSubmit);
        VertexConsumer captureBuffer = capturing
                ? ModelContactSnapshotCache.wrapActiveVertexConsumer(buffer)
                : buffer;
        try {
            original.call(model, poseStack, captureBuffer, packedLight, packedOverlay, color);
        } finally {
            if (capturing) {
                ModelContactSnapshotCache.finishLivingCapture();
            }
        }
    }
}
