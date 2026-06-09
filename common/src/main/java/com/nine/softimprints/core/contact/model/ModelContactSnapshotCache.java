package com.nine.softimprints.core.contact.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.nine.softimprints.core.contact.model.capture.ModelContactCaptureMode;
import com.nine.softimprints.core.contact.model.capture.ModelContactCapturePolicy;
import com.nine.softimprints.core.contact.model.capture.ModelContactCaptureSession;
import com.nine.softimprints.core.contact.model.capture.ModelContactMeshVertexConsumer;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshotStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ModelContactSnapshotCache {

    private static final ModelContactSnapshotStore STORE = new ModelContactSnapshotStore();
    private static final ModelContactCapturePolicy POLICY = new ModelContactCapturePolicy();
    private static final ThreadLocal<ModelContactCaptureSession> ACTIVE_SESSION = new ThreadLocal<>();
    private static final ThreadLocal<LivingEntity> BASE_MODEL_SUBMIT_ENTITY = new ThreadLocal<>();
    private static final Map<SubmitNodeStorage.ModelSubmit<?>, LivingEntity> BASE_MODEL_SUBMITS =
            new IdentityHashMap<>();

    // To cfg? Tests
    private static final long MAX_USABLE_SNAPSHOT_AGE_TICKS = 40L;
    private static final long MAX_LAST_SNAPSHOT_FALLBACK_AGE_TICKS = 80L;
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
        BASE_MODEL_SUBMITS.clear();

        frameIndex++;
    }

    public static synchronized void clear() {
        clearInternal();
    }

    private static void clearInternal() {
        STORE.clear();
        POLICY.clear();
        ACTIVE_SESSION.remove();
        BASE_MODEL_SUBMIT_ENTITY.remove();
        BASE_MODEL_SUBMITS.clear();
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

    public static ModelContactSnapshot resolveLastCompatibleSnapshot(Entity entity) {
        if (!(entity instanceof LivingEntity living)) {
            return null;
        }
        ModelContactSnapshot snapshot = STORE.latest(living.getId(), living.level().getGameTime());
        return isSnapshotCompatibleForLastSnapshotFallback(snapshot, living) ? snapshot : null;
    }

    public static double rotationFromSnapshotToCurrent(ModelContactSnapshot snapshot, Entity entity) {
        if (snapshot == null || !(entity instanceof LivingEntity living)) {
            return 0.0D;
        }
        return Math.toRadians(Mth.wrapDegrees(snapshot.captureBodyYaw() - living.yBodyRot));
    }

    private static boolean isSnapshotUsable(ModelContactSnapshot snapshot, LivingEntity entity) {
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

    private static boolean isSnapshotCompatibleForLastSnapshotFallback(ModelContactSnapshot snapshot, LivingEntity entity) {
        if (snapshot == null || snapshot.isEmpty()) {
            return false;
        }
        if (snapshot.capturePose() != entity.getPose()) {
            return false;
        }
        return entity.level().getGameTime() - snapshot.gameTime() <= MAX_LAST_SNAPSHOT_FALLBACK_AGE_TICKS;
    }

    public static void beginBaseModelSubmit(LivingEntity entity) {
        BASE_MODEL_SUBMIT_ENTITY.set(entity);
    }

    public static void finishBaseModelSubmit() {
        BASE_MODEL_SUBMIT_ENTITY.remove();
    }

    public static void markSubmittedBaseModel(RenderType renderType, SubmitNodeStorage.ModelSubmit<?> modelSubmit) {
        if (!ModelContactRenderTypes.shouldCapture(renderType)) {
            return;
        }

        LivingEntity entity = BASE_MODEL_SUBMIT_ENTITY.get();
        if (entity == null) {
            return;
        }
        BASE_MODEL_SUBMITS.put(modelSubmit, entity);
    }

    public static boolean tryBeginSubmittedBaseModelCapture(SubmitNodeStorage.ModelSubmit<?> modelSubmit) {
        LivingEntity entity = BASE_MODEL_SUBMITS.remove(modelSubmit);
        return entity != null && tryBeginLivingCapture(entity);
    }

    public static VertexConsumer wrapActiveVertexConsumer(VertexConsumer delegate) {
        ModelContactCaptureSession session = ACTIVE_SESSION.get();
        return session == null ? delegate : new ModelContactMeshVertexConsumer(delegate, session);
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
