package com.nine.softimprints.core.contact.model.capture;

import com.nine.softimprints.config.SIConfig;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ModelContactCapturePolicy {

    private final Map<Integer, ModelContactCaptureMode> modes = new ConcurrentHashMap<>();
    private final Set<Integer> immediateRequests = ConcurrentHashMap.newKeySet();

    public ModelContactCapturePolicy() {
    }

    private static int resolveInterval(ModelContactCaptureMode mode) {
        return switch (mode) {
            case OFTEN -> 1;
            default -> SIConfig.Performance.IMPRINT_SNAPSHOT_INTERVAL.get();
        };
    }

    public void setMode(
            int entityId,
            ModelContactCaptureMode mode
    ) {
        if (mode == null) {
            this.modes.remove(entityId);
            return;
        }
        this.modes.put(entityId, mode);
    }

    public void requestImmediate(int entityId) {
        this.immediateRequests.add(entityId);
    }

    public boolean consumeImmediate(int entityId) {
        return this.immediateRequests.remove(entityId);
    }

    public void forget(int entityId) {
        this.modes.remove(entityId);
        this.immediateRequests.remove(entityId);
    }

    public void clear() {
        this.modes.clear();
        this.immediateRequests.clear();
    }

    public boolean shouldCaptureThisFrame(
            int entityId,
            long frameIndex
    ) {
        if (frameIndex == 0L) {
            return false;
        }
        ModelContactCaptureMode mode = this.modes.getOrDefault(entityId, ModelContactCaptureMode.DEFAULT);
        if (mode == ModelContactCaptureMode.OFF) {
            return false;
        }
        return frameIndex % resolveInterval(mode) == 0L;
    }
}
