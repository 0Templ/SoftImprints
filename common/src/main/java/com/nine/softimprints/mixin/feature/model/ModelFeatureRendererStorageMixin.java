package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelFeatureRenderer.Storage.class)
public abstract class ModelFeatureRendererStorageMixin {

    @Inject(
            method = ModelContactMixinTargets.MODEL_FEATURE_STORAGE_ADD,
            at = @At("HEAD"),
            require = 1,
            allow = 1
    )
    private void softimprints$markBaseModelSubmit(
            RenderType renderType,
            SubmitNodeStorage.ModelSubmit<?> modelSubmit,
            CallbackInfo ci
    ) {
        ModelContactSnapshotCache.markSubmittedBaseModel(modelSubmit);
    }
}
