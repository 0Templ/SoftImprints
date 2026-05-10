package com.nine.softimprints.client.core.contact.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.client.core.contact.model.capture.ModelContactCaptureMode;
import com.nine.softimprints.client.core.contact.model.capture.ModelContactCapturePolicy;
import com.nine.softimprints.client.core.contact.model.capture.ModelContactCaptureSession;
import com.nine.softimprints.client.core.contact.model.render.OffscreenForceRenderQueue;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.client.core.contact.model.snapshot.ModelContactSnapshotStore;
import com.nine.softimprints.mixin.feature.model.ModelPartAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class ModelContactSnapshotCache {

    private static final ModelContactSnapshotStore STORE = new ModelContactSnapshotStore();
    private static final ModelContactCapturePolicy POLICY = new ModelContactCapturePolicy();
    private static final ThreadLocal<ModelContactCaptureSession> ACTIVE_SESSION = new ThreadLocal<>();

    private static long frameIndex;
    private static ClientLevel activeLevel;

    private ModelContactSnapshotCache() {
    }


    public static synchronized void onRenderFrame(boolean renderLevel) {
        if (!renderLevel) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.level != activeLevel) {
            clearInternal();
            activeLevel = client.level;
        }
        if (client.level != null) {
            STORE.prune(client.level.getGameTime());
        }

        frameIndex++;
    }

    public static synchronized void clear() {
        clearInternal();
    }

    private static void clearInternal() {
        STORE.clear();
        POLICY.clear();
        OffscreenForceRenderQueue.clear();
        ACTIVE_SESSION.remove();
        activeLevel = null;
        frameIndex = 0L;
    }

    static synchronized long currentFrameIndex() {
        return frameIndex;
    }

    public static void setCaptureMode(int entityId, ModelContactCaptureMode mode) {
        POLICY.setMode(entityId, mode);
    }

    public static void requestImmediateCapture(int entityId) {
        POLICY.requestImmediate(entityId);
    }

    public static void forgetEntity(int entityId) {
        POLICY.forget(entityId);
        STORE.remove(entityId);
    }

    public static LivingEntity resolveLivingEntity(EntityRenderState renderState) {
        if (!(renderState instanceof ModelContactRenderStateBridge bridge)) {
            return null;
        }
        Entity entity = bridge.softimprints$getEntity();
        if (!(entity instanceof LivingEntity livingEntity)) {
            return null;
        }
        if (livingEntity.isRemoved() || !livingEntity.isAlive()) {
            return null;
        }
        return livingEntity;
    }

    private static final double MAX_YAW_DELTA_RADIANS = 0.35D;

    public static ModelContactSnapshot resolveUsableSnapshot(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        ModelContactSnapshot snapshot = STORE.latest(living.getId(), living.level().getGameTime());
        return isSnapshotUsable(snapshot, living) ? snapshot : null;
    }

    public static boolean hasUsableSnapshot(Entity entity) {
        return resolveUsableSnapshot(entity) != null;
    }

    private static boolean isSnapshotUsable(ModelContactSnapshot snapshot, LivingEntity entity) {
        if (snapshot == null || snapshot.isEmpty()) {
            return false;
        }
        if (snapshot.capturePose() != entity.getPose()) {
            return false;
        }
        float captureYaw = snapshot.captureBodyYaw();
        float currentYaw = entity.yBodyRot;

        double delta = Math.toRadians(Mth.wrapDegrees(currentYaw - captureYaw));
        return Math.abs(delta) <= MAX_YAW_DELTA_RADIANS;
    }

    public static boolean isSessionActive() {
        return ACTIVE_SESSION.get() != null;
    }

    public static void captureModelPart(ModelPart modelPart, PoseStack.Pose pose) {
        ModelContactCaptureSession session = ACTIVE_SESSION.get();
        if (session == null || modelPart.skipDraw) {
            return;
        }

        List<ModelPart.Cube> cubes = ((ModelPartAccessor) (Object) modelPart).softimprints$getCubes();
        if (cubes.isEmpty()) {
            return;
        }
        session.capture(pose, cubes);
    }

    public static void beginLivingCapture(Entity entity) {
        if (!ModelContactSupport.shouldCapture(entity)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            // Todo: add proper non-living support
            return;
        }
        if (!(entity.level() instanceof ClientLevel clientLevel)) {
            return;
        }
        if (ACTIVE_SESSION.get() != null) {
            return;
        }

        int entityId = entity.getId();
        boolean immediate = POLICY.consumeImmediate(entityId);

        long currentFrame;
        synchronized (ModelContactSnapshotCache.class) {
            if (clientLevel != activeLevel) {
                clearInternal();
                activeLevel = clientLevel;
            }

            currentFrame = frameIndex;

            if (STORE.lastCapturedFrame(entityId) == currentFrame) {
                return;
            }

            if (!immediate && !POLICY.shouldCaptureThisFrame(entityId, currentFrame)) {
                return;
            }
        }

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.getMainCamera().position();
        ACTIVE_SESSION.set(new ModelContactCaptureSession(
                entityId,
                entity.level().getGameTime(),
                currentFrame,
                living.getPose(),
                living.yBodyRot,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                cameraPos.x,
                cameraPos.y,
                cameraPos.z
        ));
    }

    public static void clearLivingCapture() {
        ACTIVE_SESSION.remove();
    }

    public static void finishLivingCapture() {
        ModelContactCaptureSession session = ACTIVE_SESSION.get();
        if (session == null) {
            return;
        }
        ACTIVE_SESSION.remove();

        ModelContactSnapshot snapshot = session.build();
        if (snapshot == null || snapshot.isEmpty()) {
            return;
        }
        STORE.record(snapshot);
    }
}
