package com.nine.softimprints.core.contact.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.core.contact.model.ModelContactRenderTypes;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.core.contact.model.capture.DiscardingVertexConsumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;

final class MeshCaptureSubmitNodeCollector implements SubmitNodeCollector {

    private final Entity entity;
    private boolean capturedBaseModel;

    MeshCaptureSubmitNodeCollector(Entity entity) {
        this.entity = entity;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

    @Override
    public void submitShadow(PoseStack poseStack, float shadowRadius, List<EntityRenderState.ShadowPiece> shadowPieces) {
    }

    @Override
    public void submitNameTag(PoseStack poseStack, Vec3 offset, int order, Component text, boolean seeThrough,
                              int lightCoords, double distanceToCameraSq, CameraRenderState cameraRenderState) {
    }

    @Override
    public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence text, boolean dropShadow,
                           Font.DisplayMode displayMode, int backgroundColor, int color, int light, int order) {
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf orientation) {
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
    }

    @Override
    public <S> void submitModel(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int packedLight,
            int packedOverlay,
            int color,
            TextureAtlasSprite textureAtlasSprite,
            int outlineColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        if (this.capturedBaseModel
                || !ModelContactRenderTypes.shouldCapture(renderType)
                || !ModelContactSnapshotCache.tryBeginLivingCapture(this.entity)) {
            return;
        }
        this.capturedBaseModel = true;

        boolean complete = false;
        try {
            model.setupAnim(state);
            model.renderToBuffer(
                    poseStack,
                    ModelContactSnapshotCache.wrapActiveVertexConsumer(DiscardingVertexConsumer.INSTANCE),
                    packedLight,
                    packedOverlay,
                    color
            );
            complete = true;
        } finally {
            if (complete) {
                ModelContactSnapshotCache.finishLivingCapture();
            } else {
                ModelContactSnapshotCache.discardLivingCapture();
            }
        }
    }

    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int packedLight,
                                int packedOverlay, TextureAtlasSprite textureAtlasSprite, boolean renderWithPose,
                                boolean useTextureAtlas, int color,
                                ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int outlineColor) {
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState movingBlockRenderState) {
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, List<BlockStateModelPart> modelParts,
                                 int[] tints, int packedLight, int packedOverlay, int color) {
    }

    @Override
    public void submitBreakingBlockModel(PoseStack poseStack, BlockStateModel blockStateModel, long seed,
                                         int packedOverlay) {
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int packedLight, int packedOverlay,
                           int color, int[] tints, List<BakedQuad> quads, ItemStackRenderState.FoilType foilType) {
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType,
                                     SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer) {
    }

    @Override
    public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer particleGroupRenderer) {
    }
}
