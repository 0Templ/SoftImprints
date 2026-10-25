package com.nine.softimprints.mixin.feature.model;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.core.contact.model.render.ModelContactCaptureCollector;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Shadow;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin {
    @Shadow @Final protected List<RenderLayer<?, ?>> layers;

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
    private <S> void softimprints$submitBaseModel(
            SubmitNodeCollector collector,
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int color,
            TextureAtlasSprite sprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
            Operation<Void> original
    ) {
        LivingEntity entity = state instanceof EntityRenderState renderState
                ? ModelContactSnapshotCache.resolveLivingEntity(renderState) : null;
        // Slime's visible, physical body is the translucent outer model.
        // Todo: make this not hardcoded
        boolean outerBody = model instanceof SlimeModel && layers.stream().anyMatch(layer -> layer instanceof SlimeOuterLayer);
        if (entity == null || outerBody) {
            original.call(collector, model, state, poseStack, renderType, packedLight, packedOverlay,
                    color, sprite, outlineColor, crumblingOverlay);
            return;
        }
        ModelContactSnapshotCache.beginBaseModelSubmit(entity);
        try {
            original.call(collector, model, state, poseStack, renderType, packedLight, packedOverlay,
                    color, sprite, outlineColor, crumblingOverlay);
        } finally {
            ModelContactSnapshotCache.finishBaseModelSubmit();
        }
    }

    @WrapOperation(
            method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"
            ),
            require = 1,
            allow = 1
    )
    private void softimprints$skipLayersDuringGeometryCapture(
            RenderLayer<?, ?> layer,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int packedLight,
            EntityRenderState renderState,
            float yRot,
            float xRot,
            Operation<Void> original
    ) {
        //todo redo
        boolean body = layer instanceof SlimeOuterLayer;
        if (collector instanceof ModelContactCaptureCollector && !body) return;
        var entity = body ? ModelContactSnapshotCache.resolveLivingEntity(renderState) : null;
        if (entity == null) {
            original.call(layer, poseStack, collector, packedLight, renderState, yRot, xRot);
            return;
        }
        ModelContactSnapshotCache.beginBaseModelSubmit(entity);
        try { original.call(layer, poseStack, collector, packedLight, renderState, yRot, xRot); }
        finally { ModelContactSnapshotCache.finishBaseModelSubmit(); }
    }
}
