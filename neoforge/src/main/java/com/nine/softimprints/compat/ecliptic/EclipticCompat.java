package com.nine.softimprints.compat.ecliptic;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.*;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public final class EclipticCompat {

    private static final String HOOK_CLASS = "com.nine.softimprints.client.compat.ecliptic.EclipticCompatHooks";

    private static volatile boolean resolved;

    private static volatile MethodHandle isSnowyBlockMH;

    private static final ProfileResolver SNOWY_SURFACE_RESOLVER = EclipticCompat::resolveSnowySurface;

    private EclipticCompat() {
    }

    public static ProfileResolver snowySurfaceResolver() {
        return SNOWY_SURFACE_RESOLVER;
    }

    public static boolean isPresent() {
        ensureResolved();
        return isSnowyBlockMH != null;
    }

    public static boolean isAvailable() {
        return isPresent();
    }

    public static boolean isSnowySurface(Level level, BlockPos pos, BlockState state) {
        if (!isAvailable() || level == null || pos == null || state == null) {
            return false;
        }
        try {
            return (boolean) isSnowyBlockMH.invokeExact(level, pos, state);
        } catch (Throwable t) {
            handleHookFailure("isSnowySurface", t);
            return false;
        }
    }

    public static boolean isFullBlockState(BlockState state) {
        return isFullBlockSurface(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, state);
    }

    public static boolean isFullBlockSurface(BlockGetter level, BlockPos pos, BlockState state) {
        if (level == null || pos == null || state == null) {
            return false;
        }
        try {
            return state.isCollisionShapeFullBlock(level, pos);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private static ImprintResolveResult resolveSnowySurface(ImprintResolveContext context) {
        Level level = resolveLevel(context.level());
        if (!isFullBlockSurface(level, context.pos(), context.state())) {
            return null;
        }
        if (!isSnowySurface(level, context.pos(), context.state())) {
            return null;
        }
        return ImprintResolveResult.profile(
                SIImprintProfiles.SNOW_PROFILE,
                new ImprintRenderOverrides(SurfaceMode.OVERLAY, null, null)
        );
    }

    private static Level resolveLevel(BlockGetter level) {
        if (level instanceof Level worldLevel) {
            return worldLevel;
        }
        Minecraft mc = Minecraft.getInstance();
        return mc == null ? null : mc.level;
    }

    private static synchronized void ensureResolved() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> hookClass = Class.forName(HOOK_CLASS, true, EclipticCompat.class.getClassLoader());
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            isSnowyBlockMH = lookup.findStatic(
                    hookClass,
                    "isSnowySurface",
                    MethodType.methodType(boolean.class, Level.class, BlockPos.class, BlockState.class)
            );
        } catch (ClassNotFoundException ignored) {
            isSnowyBlockMH = null;
        } catch (Throwable t) {
            SICommon.LOGGER.warn("Failed to resolve Ecliptic compat hooks; compat disabled", t);
            isSnowyBlockMH = null;
        }
    }

    private static void handleHookFailure(String op, Throwable t) {
        synchronized (EclipticCompat.class) {
            isSnowyBlockMH = null;
        }
        SICommon.LOGGER.warn("Ecliptic compat hook '{}' failed; disabling compat for this session", op, t);
    }
}
