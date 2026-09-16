package com.nine.softimprints.core.contact.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.core.contact.model.ModelContactRenderTypes;
import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Quaternionf;

import java.util.List;

final class MeshCaptureSubmitNodeCollector implements SubmitNodeCollector {

    private final Entity entity;

    private final Vec3 cameraPos;


    private boolean capturedBaseModel;

    MeshCaptureSubmitNodeCollector(
            Entity entity,
            Vec3 cameraPos
    ) {
        this.entity = entity;
        this.cameraPos = cameraPos;
    }

    @Override
    public OrderedSubmitNodeCollector order(int order) {
        return this;
    }

    @Override
    public void submitShadow(
            PoseStack poseStack,
            float shadowRadius,
            List<EntityRenderState.ShadowPiece> shadowPieces
    ) {
    }

    @Override
    public void submitNameTag(
            PoseStack poseStack,
            Vec3 offset,
            int order,
            Component text,
            boolean seeThrough,
            int lightCoords,
            CameraRenderState cameraRenderState
    ) {
    }

    @Override
    public void submitText(
            PoseStack poseStack,
            float x,
            float y,
            FormattedCharSequence text,
            boolean dropShadow,
            Font.DisplayMode displayMode,
            int lightCoords,
            int color,
            int backgroundColor,
            int outlineColor
    ) {
    }

    @Override
    public void submitTextBackground(
            PoseStack poseStack,
            float x0,
            float y0,
            float x1,
            float y1,
            int color,
            Font.DisplayMode displayMode,
            int lightCoords
    ) {
    }

    @Override
    public void submitFlame(
            PoseStack poseStack,
            EntityRenderState renderState,
            Quaternionf orientation
    ) {
    }

    @Override
    public void submitLeash(
            PoseStack poseStack,
            EntityRenderState.LeashState leashState
    ) {
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
            UvMapping uvMapping,
            int outlineColor
    ) {
        if (this.capturedBaseModel
                || !ModelContactRenderTypes.shouldCapture(renderType)
                || !ModelContactSnapshotCache.tryBeginLivingCapture(this.entity, this.cameraPos)) {
            return;
        }
        this.capturedBaseModel = true;
        ModelContactSnapshotCache.captureModelGeometry(model, state, poseStack, packedLight, packedOverlay, color);
    }

    @Override
    public <S> void submitCrumblingOverlay(
            Model<? super S> model,
            S state,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            int tintedColor,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
    }

    @Override
    public void submitModelPart(
            ModelPart modelPart,
            PoseStack poseStack,
            RenderType renderType,
            int lightCoords,
            int overlayCoords,
            UvMapping uvMapping,
            int tintedColor,
            int outlineColor
    ) {
    }

    @Override
    public void submitMovingBlock(
            PoseStack poseStack,
            MovingBlockRenderState movingBlockRenderState,
            int outlineColor
    ) {
    }

    @Override
    public void submitBlockModel(
            PoseStack poseStack,
            RenderType renderType,
            List<BlockStateModelPart> modelParts,
            int[] tints,
            int packedLight,
            int packedOverlay,
            int color
    ) {
    }

    @Override
    public void submitBreakingBlockModel(
            PoseStack poseStack,
            List<BlockStateModelPart> parts,
            int progress,
            boolean isBlockTranslucent
    ) {
    }

    @Override
    public void submitShapeOutline(
            PoseStack poseStack,
            VoxelShape shape,
            RenderType renderType,
            int color,
            float width,
            boolean afterTerrain
    ) {
    }

    @Override
    public void submitItem(
            PoseStack poseStack,
            ItemDisplayContext displayContext,
            int packedLight,
            int packedOverlay,
            int color,
            int[] tints,
            ItemQuads quads,
            ItemStackRenderState.FoilType foilType
    ) {
    }

    @Override
    public void submitCustomGeometry(
            PoseStack poseStack,
            RenderType renderType,
            SubmitNodeCollector.CustomGeometryRenderer customGeometryRenderer
    ) {
    }

    @Override
    public void submitQuadParticleGroup(QuadParticleRenderState particles) {
    }

    @Override
    public void submitGizmoPrimitives(
            DrawableGizmoPrimitives.Group group,
            CameraRenderState camera,
            boolean onTop
    ) {
    }
}
