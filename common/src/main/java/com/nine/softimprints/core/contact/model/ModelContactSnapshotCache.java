package com.nine.softimprints.core.contact.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.contact.model.capture.*;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshotStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class ModelContactSnapshotCache {

    private static final ModelContactSnapshotStore STORE = new ModelContactSnapshotStore();
    private static final ModelContactCapturePolicy POLICY = new ModelContactCapturePolicy();
    private static final ThreadLocal<ModelContactCaptureSession> ACTIVE_SESSION = new ThreadLocal<>();

    // To cfg? Tests
    private static final long MAX_USABLE_SNAPSHOT_AGE_TICKS = 40L;
    private static final long MAX_LAST_SNAPSHOT_FALLBACK_AGE_TICKS = 80L;
    private static final double MAX_YAW_DELTA_RADIANS = 0.35D;
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
        ACTIVE_SESSION.remove();
        activeLevel = null;
        frameIndex = 0L;
    }

    static synchronized long currentFrameIndex() {
        return frameIndex;
    }

    public static void setCaptureMode(
            int entityId,
            ModelContactCaptureMode mode
    ) {
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

    public static ModelContactSnapshot resolveLastCompatibleSnapshot(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        ModelContactSnapshot snapshot = STORE.latest(living.getId(), living.level().getGameTime());
        return isSnapshotCompatibleForLastSnapshotFallback(snapshot, living) ? snapshot : null;
    }

    public static double rotationFromSnapshotToCurrent(
            ModelContactSnapshot snapshot,
            Entity entity
    ) {
        if (snapshot == null || !(entity instanceof LivingEntity living)) {
            return 0.0D;
        }
        return Math.toRadians(Mth.wrapDegrees(snapshot.captureBodyYaw() - living.yBodyRot));
    }

    private static boolean isSnapshotUsable(
            ModelContactSnapshot snapshot,
            LivingEntity entity
    ) {
        if (snapshot == null || snapshot.isEmpty()) {
            return false;
        }
        if (snapshot.capturePose() != entity.getPose()) {
            return false;
        }
        if (entity.level().getGameTime() - snapshot.gameTime() > MAX_USABLE_SNAPSHOT_AGE_TICKS) {
            return false;
        }
        float captureYaw = snapshot.captureBodyYaw();
        float currentYaw = entity.yBodyRot;

        double delta = Math.toRadians(Mth.wrapDegrees(currentYaw - captureYaw));
        return Math.abs(delta) <= MAX_YAW_DELTA_RADIANS;
    }

    private static boolean isSnapshotCompatibleForLastSnapshotFallback(
            ModelContactSnapshot snapshot,
            LivingEntity entity
    ) {
        if (snapshot == null || snapshot.isEmpty()) {
            return false;
        }
        if (snapshot.capturePose() != entity.getPose()) {
            return false;
        }
        return entity.level().getGameTime() - snapshot.gameTime() <= MAX_LAST_SNAPSHOT_FALLBACK_AGE_TICKS;
    }

    public static void captureModelGeometry(
            Model<?> model,
            PoseStack poseStack,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        captureGeometryInternal(model, null, poseStack, packedLight, packedOverlay, color);
    }

    public static <S> void captureModelGeometry(
            Model<? super S> model,
            S setupState,
            PoseStack poseStack,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        captureGeometryInternal(model, setupState, poseStack, packedLight, packedOverlay, color);
    }

    @SuppressWarnings("unchecked")
    private static void captureGeometryInternal(
            Model<?> model,
            Object setupState,
            PoseStack poseStack,
            int packedLight,
            int packedOverlay,
            int color
    ) {
        ModelContactCaptureSession session = ACTIVE_SESSION.get();
        if (session == null) {
            return;
        }

        boolean complete = false;
        try {
            if (setupState != null) {
                ((Model<Object>) model).setupAnim(setupState);
            }
            if (SIConfig.Performance.MODEL_CAPTURE_PART_TRAVERSAL.get()) {
                ModelPartObbCapturer.capture(model.root(), poseStack, session);
            } else {
                model.renderToBuffer(
                        poseStack,
                        new ModelContactMeshVertexConsumer(DiscardingVertexConsumer.INSTANCE, session),
                        packedLight,
                        packedOverlay,
                        color
                );
            }
            complete = true;
        } finally {
            if (complete) {
                finishLivingCapture();
            } else {
                discardLivingCapture();
            }
        }
    }

    public static boolean tryBeginLivingCapture(Entity entity) {
        if (!ModelContactSupport.shouldCapture(entity)) {
            return false;
        }
        if (!(entity instanceof LivingEntity living)) {
            // Todo: add proper non-living support
            return false;
        }
        if (!(entity.level() instanceof ClientLevel clientLevel)) {
            return false;
        }
        if (ACTIVE_SESSION.get() != null) {
            return false;
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
                return false;
            }

            if (!immediate && !POLICY.shouldCaptureThisFrame(entityId, currentFrame)) {
                return false;
            }
        }

        Vec3 cameraPos = Minecraft.getInstance().gameRenderer.mainCamera().position();
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
        return true;
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

    public static void discardLivingCapture() {
        ACTIVE_SESSION.remove();
    }
}
