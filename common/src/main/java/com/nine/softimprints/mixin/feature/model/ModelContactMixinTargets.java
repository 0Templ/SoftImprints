package com.nine.softimprints.mixin.feature.model;

final class ModelContactMixinTargets {

    ///
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

    static final String MODEL_PART_RENDER =
            "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;III)V";

    static final String MODEL_PART_TRANSLATE_AND_ROTATE =
            "Lnet/minecraft/client/model/geom/ModelPart;translateAndRotate(Lcom/mojang/blaze3d/vertex/PoseStack;)V";

    private ModelContactMixinTargets() {
    }
}
