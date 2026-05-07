package com.nine.softimprints.client.core.track;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;

public record MotionFrame(
        Entity entity,

        double prevX, double prevZ,
        boolean prevOnGround,
        Pose prevPose,
        float prevBodyYaw,
        double prevFallDistance,

        double curX, double curZ,
        boolean curOnGround,
        Pose curPose,
        float curBodyYaw,
        double curFallDistance
) {

    public int entityId() {
        return this.entity.getId();
    }

    public double dx() {
        return this.curX - this.prevX;
    }

    public double dz() {
        return this.curZ - this.prevZ;
    }

    public double horizontalSpeedSq() {
        double dx = this.dx();
        double dz = this.dz();
        return dx * dx + dz * dz;
    }

    public boolean landedThisTick() {
        if (this.curOnGround && !this.prevOnGround) {
            return true;
        }
        return false;
    }

    public boolean poseChanged() {
        return this.curPose != this.prevPose;
    }

    static MotionFrame seed(Entity entity) {
        double x = entity.getX();
        double z = entity.getZ();
        boolean onGround = entity.onGround();
        Pose pose = entity instanceof LivingEntity living ? living.getPose() : null;
        float bodyYaw = entity instanceof LivingEntity living ? living.yBodyRot : 0.0F;
        double fall = entity instanceof LivingEntity living ? living.fallDistance : 0.0D;
        return new MotionFrame(
                entity,
                x, z, onGround, pose, bodyYaw, fall,
                x, z, onGround, pose, bodyYaw,
                fall
        );
    }
}
