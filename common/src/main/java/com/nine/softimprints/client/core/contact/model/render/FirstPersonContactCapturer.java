package com.nine.softimprints.client.core.contact.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.client.core.contact.model.ModelContactSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public final class FirstPersonContactCapturer {

    private FirstPersonContactCapturer() {
    }

    public static void captureIfApplicable(float partialTick) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        if (!ModelContactSupport.shouldCapture(player)) {
            return;
        }
        if (!client.options.getCameraType().isFirstPerson() || client.getCameraEntity() != player) {
            return;
        }

        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        CameraRenderState cameraRenderState = client.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        if (cameraRenderState == null || !cameraRenderState.initialized) {
            return;
        }

        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();
        double renderX = Mth.lerp(partialTick, player.xo, player.getX()) - cameraPos.x;
        double renderY = Mth.lerp(partialTick, player.yo, player.getY()) - cameraPos.y;
        double renderZ = Mth.lerp(partialTick, player.zo, player.getZ()) - cameraPos.z;

        ModelContactSnapshotCache.beginLivingCapture(player);
        if (!ModelContactSnapshotCache.isSessionActive()) {
            return;
        }
        try {
            dispatcher.submit(
                    dispatcher.extractEntity(player, partialTick),
                    cameraRenderState,
                    renderX,
                    renderY,
                    renderZ,
                    new PoseStack(),
                    NoopSubmitNodeCollector.INSTANCE
            );
        } finally {
            ModelContactSnapshotCache.finishLivingCapture();
        }
    }
}
