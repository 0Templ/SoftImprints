package com.nine.softimprints.mixin.feature.model;

import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.client.core.contact.model.render.FirstPersonContactCapturer;
import com.nine.softimprints.client.core.contact.model.render.OffscreenForceRenderQueue;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {

    @Inject(
            method = ModelContactMixinTargets.GAME_RENDERER_RENDER,
            at = @At("HEAD"),
            require = 1,
            allow = 1
    )
    private void softimprints$beginRenderFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        ModelContactSnapshotCache.onRenderFrame(renderLevel);
    }

    @Inject(
            method = ModelContactMixinTargets.GAME_RENDERER_RENDER_LEVEL,
            at = @At("RETURN"),
            require = 1,
            allow = 1
    )
    private void softimprints$captureFirstPersonPlayer(DeltaTracker deltaTracker, CallbackInfo ci) {
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(true);
        FirstPersonContactCapturer.captureIfApplicable(partialTick);
        // Drain any force-render requests the game-thread driver enqueued for offscreen entities.
        // Same hook point as the first-person path: the camera / level render state is valid, and
        // we're already outside the normal entity-render critical section.
        OffscreenForceRenderQueue.drainAndCapture(partialTick);
    }
}
