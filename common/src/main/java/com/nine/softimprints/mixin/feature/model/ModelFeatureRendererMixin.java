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
            method = "renderModel(Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V",
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
        original.call(model, poseStack, buffer, packedLight, packedOverlay, color);

        if (!ModelContactSnapshotCache.tryBeginSubmittedBaseModelCapture(modelSubmit)) {
            return;
        }
        ModelContactSnapshotCache.captureModelGeometry(model, poseStack, packedLight, packedOverlay, color);
    }
}
