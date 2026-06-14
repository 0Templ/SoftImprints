package com.nine.softimprints.mixin.feature.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.core.contact.model.ModelContactRenderTypes;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
                    ordinal = 0
            ),
            require = 1,
            allow = 1
    )
    private <S> void softimprints$captureBaseModelSubmit(
            SubmitNodeCollector submitNodeCollector,
            Model<? super S> model,
            S modelState,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int color,
            TextureAtlasSprite textureAtlasSprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original,
            LivingEntityRenderState renderState,
            PoseStack outerPoseStack,
            SubmitNodeCollector outerSubmitNodeCollector,
            CameraRenderState cameraRenderState
    ) {
        original.call(
                submitNodeCollector,
                model,
                modelState,
                poseStack,
                renderType,
                packedLight,
                packedOverlay,
                color,
                textureAtlasSprite,
                outlineColor,
                crumblingOverlay
        );

        LivingEntity entity = ModelContactSnapshotCache.resolveLivingEntity(renderState);
        if (entity == null
                || !ModelContactRenderTypes.shouldCapture(renderType)
                || !ModelContactSnapshotCache.tryBeginLivingCapture(entity)) {
            return;
        }
        ModelContactSnapshotCache.captureModelGeometry(model, modelState, poseStack, packedLight, packedOverlay, color);
    }
}
