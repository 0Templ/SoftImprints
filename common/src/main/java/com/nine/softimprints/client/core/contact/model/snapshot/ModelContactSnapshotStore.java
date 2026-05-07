package com.nine.softimprints.client.core.contact.model.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.Iterator;

public final class ModelContactSnapshotStore {

    private static final int MAX_SNAPSHOT_AGE_TICKS = 1200;

    private final Int2ObjectOpenHashMap<SnapshotState> snapshots = new Int2ObjectOpenHashMap<>();

    public ModelContactSnapshotStore() {
    }

    public synchronized void record(ModelContactSnapshot snapshot) {
        SnapshotState state = this.snapshots.get(snapshot.entityId());
        if (state == null) {
            state = new SnapshotState();
            this.snapshots.put(snapshot.entityId(), state);
        }
        state.record(snapshot);
    }

    public synchronized ModelContactSnapshot latest(int entityId, long currentGameTime) {
        SnapshotState state = this.snapshots.get(entityId);
        if (state == null) {
            return null;
        }
        ModelContactSnapshot snapshot = state.peekLatest(currentGameTime);
        if (snapshot == null) {
            this.snapshots.remove(entityId);
        }
        return snapshot;
    }

    public synchronized long lastCapturedFrame(int entityId) {
        SnapshotState state = this.snapshots.get(entityId);
        return state == null ? Long.MIN_VALUE : state.lastCapturedFrame;
    }

    public synchronized void remove(int entityId) {
        this.snapshots.remove(entityId);
    }

    public synchronized void clear() {
        this.snapshots.clear();
    }

    public synchronized void prune(long currentGameTime) {
        Iterator<Int2ObjectMap.Entry<SnapshotState>> it = this.snapshots.int2ObjectEntrySet().fastIterator();
        while (it.hasNext()) {
            Int2ObjectMap.Entry<SnapshotState> entry = it.next();
            if (!entry.getValue().isItFresh(currentGameTime)) {
                it.remove();
            }
        }
    }

    private static final class SnapshotState {

        long lastCapturedFrame = Long.MIN_VALUE;
        long lastCapturedGameTime = Long.MIN_VALUE;


        ModelContactSnapshot latest;

        void record(ModelContactSnapshot snapshot) {
            this.lastCapturedFrame = snapshot.frameIndex();
            this.lastCapturedGameTime = snapshot.gameTime();
            this.latest = snapshot;
        }

        ModelContactSnapshot peekLatest(long currentGameTime) {
            if (!isItFresh(currentGameTime)) {
                this.latest = null;
                return null;
            }
            return this.latest;
        }

        boolean isItFresh(long currentGameTime) {
            return this.latest != null && (currentGameTime - this.lastCapturedGameTime) <= MAX_SNAPSHOT_AGE_TICKS;
        }
    }
}
