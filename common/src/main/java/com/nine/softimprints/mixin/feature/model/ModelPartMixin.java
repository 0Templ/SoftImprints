package com.nine.softimprints.mixin.feature.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelPart.class)
public abstract class ModelPartMixin {

    @Inject(
            method = ModelContactMixinTargets.MODEL_PART_RENDER,
            at = @At(
                    value = "INVOKE",
                    target = ModelContactMixinTargets.MODEL_PART_TRANSLATE_AND_ROTATE,
                    ordinal = 0,
                    shift = At.Shift.AFTER
            ),
            require = 1,
            allow = 1
    )
    private void softimprints$captureModelPart(
            PoseStack poseStack,
            VertexConsumer buffer,
            int packedLight,
            int packedOverlay,
            int color,
            CallbackInfo ci
    ) {
        ModelContactSnapshotCache.captureModelPart((ModelPart) (Object) this, poseStack.last());
    }
}
