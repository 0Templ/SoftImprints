package com.nine.softimprints.client.core.contact.model.capture;

import com.nine.softimprints.client.core.contact.model.ModelContactSnapshotCache;
import com.nine.softimprints.client.core.contact.model.ModelContactSupport;
import com.nine.softimprints.client.core.contact.model.render.OffscreenForceRenderQueue;
import com.nine.softimprints.client.core.track.MotionFrame;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import static com.nine.softimprints.client.core.contact.model.capture.ModelContactCaptureMode.OFTEN;

public final class ModelContactCaptureDriver {

    private static final double TELEPORT_DELTA_SQ = 1.0D;

    private static final int OFTEN_HOLD_TICKS = 4;

    private static final double TURN_EDGE_RADIANS = 0.25D;

    private final Int2ObjectOpenHashMap<CaptureState> states = new Int2ObjectOpenHashMap<>();

    public void clear() {
        this.states.clear();
        OffscreenForceRenderQueue.clear();
    }

    public void tick(Iterable<MotionFrame> frames) {
        IntSet seen = new IntOpenHashSet();
        for (MotionFrame frame : frames) {
            seen.add(frame.entityId());
            evaluate(frame);
        }
        this.states.keySet().removeIf((int id) -> {
            if (seen.contains(id)) {
                return false;
            }
            ModelContactSnapshotCache.forgetEntity(id);
            return true;
        });
    }

    private void evaluate(MotionFrame frame) {
        Entity entity = frame.entity();
        if (!ModelContactSupport.shouldUseModelContact(entity)) {
            int id = frame.entityId();
            if (this.states.remove(id) != null) {
                ModelContactSnapshotCache.forgetEntity(id);
            }
            return;
        }

        int id = frame.entityId();
        CaptureState state = this.states.get(id);
        boolean bootstrap = (state == null);
        if (bootstrap) {
            state = new CaptureState();
            this.states.put(id, state);
            ModelContactSnapshotCache.requestImmediateCapture(id);
            requestOffscreenCapture(id);
        }

        boolean poseChanged = frame.poseChanged();
        boolean teleported = frame.horizontalSpeedSq() > TELEPORT_DELTA_SQ;
        boolean turnedHard = isTurnHard(frame);

        boolean justLanded = frame.landedThisTick();

        boolean edge = justLanded || poseChanged || teleported || turnedHard;

        if (edge) {
            if (justLanded || poseChanged || teleported) {
                ModelContactSnapshotCache.forgetEntity(id);
            }
            state.oftenHoldTicks = OFTEN_HOLD_TICKS;
            ModelContactSnapshotCache.requestImmediateCapture(id);
            requestOffscreenCapture(id);
        } else {
            ModelContactSnapshotCache.requestImmediateCapture(id);
        }

        if (!ModelContactSnapshotCache.hasUsableSnapshot(entity)) {
            ModelContactSnapshotCache.requestImmediateCapture(id);
            requestOffscreenCapture(id);
        }

        ModelContactCaptureMode mode = resolveMode(frame, state);
        if (bootstrap || mode != state.lastPublishedMode) {
            ModelContactSnapshotCache.setCaptureMode(id, mode);
            state.lastPublishedMode = mode;
        }
    }

    private static boolean isTurnHard(MotionFrame frame) {
        if (!(frame.entity() instanceof LivingEntity)) {
            return false;
        }
        double deltaRadians = Math.abs(
                Math.toRadians((double) Mth.wrapDegrees(frame.curBodyYaw() - frame.prevBodyYaw()))
        );
        return deltaRadians > TURN_EDGE_RADIANS;
    }

    private static ModelContactCaptureMode resolveMode(MotionFrame frame, CaptureState state) {
        boolean airborne = !frame.curOnGround();
        if (airborne) {
            state.oftenHoldTicks = OFTEN_HOLD_TICKS;
            return OFTEN;
        }
        if (state.oftenHoldTicks > 0) {
            state.oftenHoldTicks--;
            return OFTEN;
        }
        return ModelContactCaptureMode.DEFAULT;
    }

    private static void requestOffscreenCapture(int id) {
        if (ModelContactSupport.shouldRequestOffscreenCapture()) {
            OffscreenForceRenderQueue.request(id);
        }
    }

    private static final class CaptureState {
        int oftenHoldTicks;
        ModelContactCaptureMode lastPublishedMode;
    }
}
