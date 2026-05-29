package com.nine.softimprints.core.contact.model;

import com.nine.softimprints.config.ModelContactFallbackPolicy;
import com.nine.softimprints.config.SIConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class ModelContactSupport {

    private ModelContactSupport() {
    }

    private static boolean isEnabled() {
        return SIConfig.General.IMPRINT_MODEL_CONTACT.get();
    }

    private static boolean isTrackedEntity(Entity entity) {
        return entity != null
                && entity.isAlive()
                && !entity.isRemoved()
                && !entity.isSpectator()
                && entity.getBoundingBox().getXsize() > 0.0D
                && entity.getBoundingBox().getZsize() > 0.0D;
    }

    public static boolean shouldUseModelContact(Entity entity) {
        return isEnabled()
                && entity instanceof LivingEntity
                && isTrackedEntity(entity);
    }

    public static boolean shouldFallbackToBoundingBox() {
        return SIConfig.General.MODEL_CONTACT_FALLBACK_POLICY.get() == ModelContactFallbackPolicy.BOUNDING_BOX;
    }

    public static boolean shouldFallbackToLastSnapshot() {
        return SIConfig.General.MODEL_CONTACT_FALLBACK_POLICY.get() == ModelContactFallbackPolicy.LAST_SNAPSHOT;
    }

    public static boolean shouldCapture(Entity entity) {
        return shouldUseModelContact(entity);
    }

    public static double resolveBandHeight() {
        return Math.max(1.0E-4D, SIConfig.General.IMPRINT_CONTACT_BAND_HEIGHT.get());
    }
}
