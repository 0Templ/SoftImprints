package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.core.contact.model.render.FirstPersonContactCapturer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "render()V",
            at = @At("HEAD"),
            require = 1,
            allow = 1
    )
    private void softimprints$beginRenderFrame(CallbackInfo ci) {
        ModelContactSnapshotCache.onRenderFrame(
                Minecraft.getInstance().gameRenderer.gameRenderState().shouldRenderLevel
        );
    }

    @Inject(
            method = "renderLevel()V",
            at = @At("RETURN"),
            require = 1,
            allow = 1
    )
    private void softimprints$captureFirstPersonPlayer(CallbackInfo ci) {
        FirstPersonContactCapturer.captureIfApplicable(
                Minecraft.getInstance().gameRenderer.gameRenderState().levelRenderState.worldPartialTicks
        );
    }
}
