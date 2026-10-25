package com.nine.softimprints.core.contact.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.contact.model.capture.*;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshot;
import com.nine.softimprints.core.contact.model.snapshot.ModelContactSnapshotStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Map;

public final class ModelContactSnapshotCache {

    private static final ModelContactSnapshotStore STORE = new ModelContactSnapshotStore();
    private static final ModelContactCapturePolicy POLICY = new ModelContactCapturePolicy();
    private static final ThreadLocal<ModelContactCaptureSession> ACTIVE_SESSION = new ThreadLocal<>();
    private record RenderOrigin(LivingEntity entity, Matrix4f pose) {}
    private record ModelSubject(LivingEntity entity, Matrix4f localPose) {}
    private static final ThreadLocal<Deque<RenderOrigin>> RENDER_ORIGINS = new ThreadLocal<>();
    private static final ThreadLocal<Deque<LivingEntity>> BASE_MODEL_SUBMITS_ACTIVE = new ThreadLocal<>();
    private static final Map<SubmitNodeStorage.ModelSubmit<?>, ModelSubject> BASE_MODEL_SUBMITS = new IdentityHashMap<>();

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
        if (client.isPaused()) {
            return;
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
        BASE_MODEL_SUBMITS_ACTIVE.remove();
        RENDER_ORIGINS.remove();
        BASE_MODEL_SUBMITS.clear();
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
        if (!snapshot.matchesDimensions(entity.getBbWidth(), entity.getBbHeight())) return false;
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
        if (!snapshot.matchesDimensions(entity.getBbWidth(), entity.getBbHeight())) return false;
        if (snapshot.capturePose() != entity.getPose()) {
            return false;
        }
        return entity.level().getGameTime() - snapshot.gameTime() <= MAX_LAST_SNAPSHOT_FALLBACK_AGE_TICKS;
    }

    public static void beginEntityRender(LivingEntity entity, PoseStack poses) {
        var origins = RENDER_ORIGINS.get();
        if (origins == null) { origins = new ArrayDeque<>(); RENDER_ORIGINS.set(origins); }
        origins.push(new RenderOrigin(entity, new Matrix4f(poses.last().pose())));
    }

    public static void finishEntityRender() {
        var origins = RENDER_ORIGINS.get();
        if (origins == null || origins.isEmpty()) return;
        origins.pop();
        if (origins.isEmpty()) RENDER_ORIGINS.remove();
    }

    public static void beginBaseModelSubmit(LivingEntity entity) {
        var submissions = BASE_MODEL_SUBMITS_ACTIVE.get();
        if (submissions == null) { submissions = new ArrayDeque<>(); BASE_MODEL_SUBMITS_ACTIVE.set(submissions); }
        submissions.push(entity);
    }

    public static void finishBaseModelSubmit() {
        var submissions = BASE_MODEL_SUBMITS_ACTIVE.get();
        if (submissions == null || submissions.isEmpty()) return;
        submissions.pop();
        if (submissions.isEmpty()) BASE_MODEL_SUBMITS_ACTIVE.remove();
    }

    public static boolean isBaseModelSubmitFor(Entity entity) {
        return currentBaseModelSubmitEntity() == entity;
    }

    private static LivingEntity currentBaseModelSubmitEntity() {
        var subjects = BASE_MODEL_SUBMITS_ACTIVE.get();
        return subjects == null ? null : subjects.peek();
    }

    private static Matrix4f relativePose(Entity entity, PoseStack.Pose submitted) {
        var origins = RENDER_ORIGINS.get();
        var origin = origins == null ? null : origins.peek();
        if (origin == null || origin.entity() != entity) return null;
        try { return ModelContactPose.relative(origin.pose(), submitted.pose()); }
        catch (IllegalArgumentException ignored) { return null; }
    }

    public static void markSubmittedBaseModel(
            RenderType renderType,
            SubmitNodeStorage.ModelSubmit<?> modelSubmit
    ) {
        if (!ModelContactRenderTypes.shouldCapture(renderType)) {
            return;
        }

        var entity = currentBaseModelSubmitEntity();
        if (entity == null) return;
        var local = relativePose(entity, modelSubmit.pose());
        if (local != null) BASE_MODEL_SUBMITS.put(modelSubmit, new ModelSubject(entity, local));
    }

    public static boolean tryBeginSubmittedBaseModelCapture(SubmitNodeStorage.ModelSubmit<?> modelSubmit) {
        var subject = BASE_MODEL_SUBMITS.get(modelSubmit);
        if (subject == null) {
            return false;
        }
        if (!tryBeginLivingCapture(subject.entity(), subject.localPose())) {
            return false;
        }
        BASE_MODEL_SUBMITS.remove(modelSubmit);
        return true;
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
        ModelPoseGuard guard = setupState == null ? null : new ModelPoseGuard(model.root());
        poseStack.pushPose();
        try {
            poseStack.last().pose().set(session.modelPose());
            poseStack.last().normal().identity();
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
        } catch (RuntimeException ignored) {}
        finally {
            poseStack.popPose();
            if (guard != null) guard.close();
            if (complete) {
                finishLivingCapture();
            } else {
                discardLivingCapture();
            }
        }
    }

    public static boolean tryBeginLivingCapture(Entity entity, PoseStack poses) {
        if (!isBaseModelSubmitFor(entity)) return false;
        var local = relativePose(entity, poses.last());
        return local != null && tryBeginLivingCapture(entity, local);
    }

    private static boolean tryBeginLivingCapture(Entity entity, Matrix4f localPose) {
        // Do not consume pending capture requests while the world is paused.
        if (Minecraft.getInstance().isPaused()) {
            return false;
        }
        if (!ModelContactSupport.shouldCapture(entity)) {
            return false;
        }
        if (!(entity instanceof LivingEntity living)) {
            // Todo: add proper non-living support!!!!!
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

        ACTIVE_SESSION.set(new ModelContactCaptureSession(
                entityId,
                entity.level().getGameTime(),
                currentFrame,
                living.getPose(),
                living.yBodyRot, localPose, living.getBbWidth(), living.getBbHeight()
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
