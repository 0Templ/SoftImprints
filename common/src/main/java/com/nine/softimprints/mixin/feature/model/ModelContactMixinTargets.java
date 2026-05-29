package com.nine.softimprints.mixin.feature.model;


// TODO: It's actually not very convenient. Move strings directly into mixin classes
final class ModelContactMixinTargets {

    static final String ENTITY_RENDERER_EXTRACT_RENDER_STATE =
            "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V";

    static final String LIVING_ENTITY_RENDERER_EXTRACT_RENDER_STATE =
            "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V";

    static final String GAME_RENDERER_RENDER =
            "render(Lnet/minecraft/client/DeltaTracker;Z)V";

    static final String GAME_RENDERER_RENDER_LEVEL =
            "renderLevel(Lnet/minecraft/client/DeltaTracker;)V";

    static final String LIVING_ENTITY_RENDERER_SUBMIT =
            "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V";

    static final String SUBMIT_NODE_COLLECTOR_SUBMIT_MODEL =
            "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V";

    static final String MODEL_FEATURE_STORAGE_ADD =
            "add(Lnet/minecraft/client/renderer/rendertype/RenderType;Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;)V";

    static final String MODEL_FEATURE_RENDER_MODEL =
            "renderModel(Lnet/minecraft/client/renderer/SubmitNodeStorage$ModelSubmit;Lnet/minecraft/client/renderer/rendertype/RenderType;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/renderer/OutlineBufferSource;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;)V";

    static final String MODEL_RENDER_TO_BUFFER =
            "Lnet/minecraft/client/model/Model;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V";

    private ModelContactMixinTargets() {
    }
}
