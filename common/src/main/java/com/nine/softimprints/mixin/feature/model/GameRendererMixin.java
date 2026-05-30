package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.core.contact.model.render.FirstPersonContactCapturer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At("HEAD"),
            require = 1,
            allow = 1
    )
    private void softimprints$beginRenderFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ModelContactSnapshotCache.onRenderFrame(renderLevel);
    }

    @Inject(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At("RETURN"),
            require = 1,
            allow = 1
    )
    private void softimprints$captureFirstPersonPlayer(DeltaTracker deltaTracker, CallbackInfo ci) {
        FirstPersonContactCapturer.captureIfApplicable(deltaTracker.getGameTimeDeltaPartialTick(true));
    }
}
