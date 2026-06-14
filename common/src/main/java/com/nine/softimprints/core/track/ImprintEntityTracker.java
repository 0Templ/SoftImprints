package com.nine.softimprints.core.track;

import com.nine.softimprints.config.SIConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class ImprintEntityTracker {

    private static final double ABSOLUTE_MIN_STEP = 0.05D;

    private static final int SETTLE_TRACK_TICKS = 60;
    private static final int POSE_RESTAMP_INTERVAL = 10;

    private final Int2ObjectOpenHashMap<StampState> states = new Int2ObjectOpenHashMap<>();

    private static StampTrigger classifyTrigger(
            MotionFrame frame,
            StampState state
    ) {
        if (frame.landedThisTick()) {
            return StampTrigger.LANDING;
        }
        double dx = frame.curX() - state.anchorX;
        double dz = frame.curZ() - state.anchorZ;
        double distFromAnchor = Math.sqrt(dx * dx + dz * dz);
        double stepDistance = resolveStepDistance(frame.entity());
        boolean stationary = distFromAnchor < stepDistance * 0.5;

        if (frame.curOnGround()) {
            if (frame.poseChanged() && stationary) {
                state.poseSettleTicks = SETTLE_TRACK_TICKS;
                return StampTrigger.POSE_CHANGED;
            }
            if (distFromAnchor >= stepDistance) {
                return StampTrigger.STEP;
            }
            double rotateDelta = Math.abs(Mth.wrapDegrees(frame.curBodyYaw() - state.anchorYaw));

            if (rotateDelta >= SIConfig.General.IMPRINT_ROTATION_STEP_DEGREES.get()) {
                return StampTrigger.ROTATED;
            }
        }
        if (state.poseSettleTicks > 0) {
            state.poseSettleTicks--;
            if (!stationary || !frame.curOnGround()) {
                state.poseSettleTicks = 0;
            } else if (state.poseSettleTicks % POSE_RESTAMP_INTERVAL == 0) {
                return StampTrigger.POSE_SETTLING;
            }
        }
        return StampTrigger.NONE;
    }

    private static void apply(
            StampState state,
            MotionFrame frame,
            StampTrigger trigger
    ) {
        switch (trigger) {

            case LANDING -> {
                state.anchorYaw = frame.curBodyYaw();

                state.anchorX = frame.curX();
                state.anchorZ = frame.curZ();
                state.poseSettleTicks = 0;

                state.pending = true;
            }
            case STEP -> {
                state.anchorYaw = frame.curBodyYaw();

                double dx = frame.curX() - state.anchorX;
                double dz = frame.curZ() - state.anchorZ;
                double distFromAnchor = Math.sqrt(dx * dx + dz * dz);
                double stepDistance = resolveStepDistance(frame.entity());
                double inv = 1.0D / distFromAnchor;
                state.anchorX += dx * inv * stepDistance;
                state.anchorZ += dz * inv * stepDistance;
                state.poseSettleTicks = 0;
                state.pending = true;
            }
            case ROTATED -> {
                state.anchorYaw = frame.curBodyYaw();
                state.poseSettleTicks = 0;
                state.pending = true;
            }
            case POSE_CHANGED, POSE_SETTLING -> {
                state.anchorX = frame.curX();
                state.anchorZ = frame.curZ();
                state.anchorYaw = frame.curBodyYaw();
                state.pending = true;
            }
            case NONE -> {

            }
        }
    }

    private static double resolveStepDistance(Entity entity) {
        double configured = SIConfig.General.IMPRINT_STEP_DISTANCE.get();

        double baseScale = 1;

        double width = entity.getBoundingBox().getXsize();
        double depth = entity.getBoundingBox().getZsize();
        double footprint = Math.max(width, depth);
        double widthBased = Math.max(ABSOLUTE_MIN_STEP, footprint * 0.25D * baseScale);
        return Math.max(configured, widthBased);
    }

    public void clear() {
        this.states.clear();
    }

    public void tick(Iterable<MotionFrame> frames) {
        IntSet seen = new IntOpenHashSet();
        for (MotionFrame frame : frames) {
            seen.add(frame.entityId());
            advance(frame);
        }
        this.states.keySet().removeIf((int id) -> !seen.contains(id));
    }

    public IntSet pendingIds() {
        IntSet result = new IntOpenHashSet();
        for (var entry : this.states.int2ObjectEntrySet()) {
            if (entry.getValue().pending) {
                result.add(entry.getIntKey());
            }
        }
        return result;
    }

    /**
     * Clears the pending flag for ids the write pipeline confirmed it stamped
     */
    public void ackApplied(IntSet applied) {
        if (applied.isEmpty()) {
            return;
        }
        for (int id : applied) {
            StampState state = this.states.get(id);
            if (state != null) {
                state.pending = false;
            }
        }
    }

    private void advance(MotionFrame frame) {
        int id = frame.entityId();
        StampState state = this.states.get(id);
        if (state == null) {
            bootstrap(id, frame);
            return;
        }

        StampTrigger trigger = classifyTrigger(frame, state);
        apply(state, frame, trigger);
    }

    private void bootstrap(
            int id,
            MotionFrame frame
    ) {
        StampState state = new StampState();
        state.anchorX = frame.curX();
        state.anchorZ = frame.curZ();
        state.anchorYaw = frame.curBodyYaw();
        this.states.put(id, state);
    }

    private enum StampTrigger {
        NONE,
        LANDING,
        ROTATED,
        POSE_CHANGED,
        POSE_SETTLING,
        STEP
    }

    private static final class StampState {
        double anchorX;
        double anchorZ;
        double anchorYaw;

        int poseSettleTicks;

        boolean pending;
    }
}
