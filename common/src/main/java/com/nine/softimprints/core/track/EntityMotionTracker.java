package com.nine.softimprints.core.track;

import com.nine.softimprints.config.SIConfig;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

import java.util.Collection;


public final class EntityMotionTracker {

    private final Int2ObjectOpenHashMap<MotionState> states = new Int2ObjectOpenHashMap<>();

    private final Int2ObjectOpenHashMap<MotionFrame> frames = new Int2ObjectOpenHashMap<>();

    private static boolean validForTrack(
            Entity entity,
            EntityTargetFilter.Snapshot targetFilter
    ) {
        return entity.isAlive()
                && !entity.isRemoved()
                && !entity.isSpectator()
                && targetFilter.allows(entity)
                && entity.getBoundingBox().getXsize() > 0.0D
                && entity.getBoundingBox().getZsize() > 0.0D;
    }

    private static Pose resolvePose(Entity entity) {
        return entity instanceof LivingEntity living ? living.getPose() : null;
    }

    private static float resolveBodyYaw(Entity entity) {
        return entity instanceof LivingEntity living ? living.yBodyRot : 0.0F;
    }

    private static double resolveFallDistance(Entity entity) {
        return entity instanceof LivingEntity living ? living.fallDistance : 0.0D;
    }

    public void clear() {
        this.states.clear();
        this.frames.clear();
    }

    public void tick(
            ClientLevel level,
            Entity observer
    ) {
        this.frames.clear();

        var observerPos = observer.blockPosition();

        EntityTargetFilter.Snapshot targetFilter = EntityTargetFilter.current();
        IntSet seen = new IntOpenHashSet();

        var limDistCfg = SIConfig.Performance.ENTITIES_TRACK_RANGE;
        int reqDist = limDistCfg.get();
        boolean distUnlimited = reqDist == limDistCfg.max().intValue();

        var limCfg = SIConfig.Performance.MAX_TRACK_ENTITIES;
        int maxTracked = limCfg.get();
        boolean amountUnlimited = maxTracked == limCfg.max().intValue();
        int tracked = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!amountUnlimited && tracked >= maxTracked) {
                break;
            }
            if (!distUnlimited) {
                double dx = entity.getX() - observerPos.getX();
                double dz = entity.getZ() - observerPos.getZ();
                if (!(dx * dx + dz * dz <= reqDist * reqDist)) continue;
            }

            if (observe(entity, seen, targetFilter)) {
                tracked++;
            }

        }

        observe(observer, seen, targetFilter);

        this.states.keySet().removeIf((int id) -> !seen.contains(id));
    }

    public Collection<MotionFrame> frames() {
        return this.frames.values();
    }

    public MotionFrame frameOf(int entityId) {
        return this.frames.get(entityId);
    }

    private boolean observe(
            Entity entity,
            IntSet seen,
            EntityTargetFilter.Snapshot targetFilter
    ) {
        if (entity == null) {
            return false;
        }
        int id = entity.getId();
        if (!seen.add(id)) {
            return false;
        }
        if (!validForTrack(entity, targetFilter)) {
            this.states.remove(id);
            return false;
        }

        MotionState state = this.states.get(id);
        double curX = entity.getX();
        double curZ = entity.getZ();
        boolean curOnGround = entity.onGround();
        Pose curPose = resolvePose(entity);
        float curBodyYaw = resolveBodyYaw(entity);
        double curFallDistance = resolveFallDistance(entity);

        MotionFrame frame;
        if (state == null) {
            state = new MotionState();
            this.states.put(id, state);
            frame = MotionFrame.seed(entity);
        } else {
            frame = new MotionFrame(
                    entity,
                    state.lastX, state.lastZ, state.lastOnGround, state.lastPose, state.lastBodyYaw, state.lastFallDistance,
                    curX, curZ, curOnGround, curPose, curBodyYaw,
                    curFallDistance
            );
        }

        state.lastX = curX;
        state.lastZ = curZ;
        state.lastOnGround = curOnGround;
        state.lastPose = curPose;
        state.lastBodyYaw = curBodyYaw;
        state.lastFallDistance = curFallDistance;

        this.frames.put(id, frame);
        return true;
    }

    private static final class MotionState {
        double lastX;
        double lastZ;
        boolean lastOnGround;
        Pose lastPose;
        float lastBodyYaw;
        double lastFallDistance;
    }
}
