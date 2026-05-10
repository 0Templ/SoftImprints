package com.nine.softimprints.client.core.contact.model.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.client.core.contact.model.ModelContactSupport;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class OffscreenForceRenderQueue {

    private static final Object LOCK = new Object();
    private static IntSet pending = new IntOpenHashSet();

    private OffscreenForceRenderQueue() {
    }

    public static void request(int entityId) {
        synchronized (LOCK) {
            pending.add(entityId);
        }
    }

    public static void clear() {
        synchronized (LOCK) {
            pending.clear();
        }
    }

    public static void drainAndCapture(float partialTick) {
        IntSet drained;
        synchronized (LOCK) {
            if (pending.isEmpty()) {
                return;
            }
            drained = pending;
            pending = new IntOpenHashSet();
        }

        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (level == null) {
            requeue(drained);
            return;
        }

        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();
        CameraRenderState cameraRenderState = client.gameRenderer.getGameRenderState().levelRenderState.cameraRenderState;
        if (cameraRenderState == null || !cameraRenderState.initialized) {
            requeue(drained);
            return;
        }
        Vec3 cameraPos = client.gameRenderer.getMainCamera().position();

        int maxPerFrame = SIConfig.Performance.MAX_OFFSCREEN_CAPTURES_PER_FRAME.get();
        if (maxPerFrame <= 0) {
            return;
        }

        int processed = 0;
        IntSet leftovers = null;
        IntIterator iterator = drained.iterator();
        while (iterator.hasNext()) {
            int entityId = iterator.nextInt();
            if (processed >= maxPerFrame) {
                if (leftovers == null) {
                    leftovers = new IntOpenHashSet();
                }
                leftovers.add(entityId);
                while (iterator.hasNext()) {
                    leftovers.add(iterator.nextInt());
                }
                break;
            }
            Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity living)) {
                continue;
            }
            if (!ModelContactSupport.shouldCapture(living)) {
                continue;
            }

            double renderX = Mth.lerp(partialTick, living.xo, living.getX()) - cameraPos.x;
            double renderY = Mth.lerp(partialTick, living.yo, living.getY()) - cameraPos.y;
            double renderZ = Mth.lerp(partialTick, living.zo, living.getZ()) - cameraPos.z;

            ModelContactSnapshotCache.beginLivingCapture(living);
            if (!ModelContactSnapshotCache.isSessionActive()) {
                continue;
            }
            try {
                dispatcher.submit(
                        dispatcher.extractEntity(living, partialTick),
                        cameraRenderState,
                        renderX,
                        renderY,
                        renderZ,
                        new PoseStack(),
                        NoopSubmitNodeCollector.INSTANCE
                );
            }
            catch (Exception e) {
                ModelContactSnapshotCache.clearLivingCapture();
                return;
            }
            finally {
                ModelContactSnapshotCache.finishLivingCapture();
            }
            processed++;
        }

        if (leftovers != null && !leftovers.isEmpty()) {
            requeue(leftovers);
        }
    }

    private static void requeue(IntSet entityIds) {
        synchronized (LOCK) {
            pending.addAll(entityIds);
        }
    }
}
