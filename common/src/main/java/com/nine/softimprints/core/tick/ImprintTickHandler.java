package com.nine.softimprints.core.tick;

import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.ImprintProcessor;
import com.nine.softimprints.core.cache.CacheAccess;
import com.nine.softimprints.core.cache.ImprintCache;
import com.nine.softimprints.core.cache.LevelData;
import com.nine.softimprints.core.contact.model.capture.ModelContactCaptureDriver;
import com.nine.softimprints.core.track.EntityMotionTracker;
import com.nine.softimprints.core.track.ImprintEntityTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.SectionPos;

import java.util.List;

public class ImprintTickHandler {

    private static final EntityMotionTracker motionTracker = new EntityMotionTracker();
    private static final ImprintEntityTracker stampTracker = new ImprintEntityTracker();
    private static final ModelContactCaptureDriver modelCaptureDriver = new ModelContactCaptureDriver();
    private static ClientLevel level;
    private static long tickCounter;

    public static void tick(Minecraft client) {
        if (!prepareTick(client)) {
            return;
        }

        motionTracker.tick(level, client.player);
        var frames = motionTracker.frames();

        {
            stampTracker.tick(frames);
            modelCaptureDriver.tick(frames);
        }

        LevelData levelData = CacheAccess.current();
        if (levelData == null) return;

        ImprintCache cache = levelData.getImprintCache();
        if (cache == null) return;
        if (shouldTick(tickCounter, SIConfig.Performance.IMPRINT_WRITE_CHECK_TICK_RATE.get())) {
            var ids = stampTracker.pendingIds();
            if (!ids.isEmpty()) {
                var result = ImprintProcessor.getBlockMasks(level, ids);
                cache.apply(result.masks(), level.getGameTime());
                stampTracker.ackApplied(result.applied());
            }
        }

        if (shouldTick(tickCounter, SIConfig.Performance.IMPRINT_CHUNK_REBUILD_TICK_RATE.get())) {
            var cfg = SIConfig.Performance.MAX_SECTIONS_REBUILD_PER_ITERATION;
            int amount = cfg.get();
            rebuildSections(client, amount == cfg.max().intValue() ? cache.drainAllSections() : cache.drainSections(amount));
        }
    }

    private static boolean shouldTick(
            long value,
            long interval
    ) {
        return value % interval == 0L;
    }

    private static void clearRunState() {
        tickCounter = 0L;
        motionTracker.clear();
        stampTracker.clear();
        modelCaptureDriver.clear();
    }

    private static void rebuildSections(
            Minecraft client,
            List<Long> dirtySections
    ) {
        if (dirtySections.isEmpty()) {
            return;
        }
        dirtySections.forEach(section ->
                client.levelRenderer.setSectionDirty(
                        SectionPos.x(section),
                        SectionPos.y(section),
                        SectionPos.z(section)
                )
        );
    }

    private static boolean prepareTick(Minecraft client) {
        if (client == null) {
            return false;
        }

        ClientLevel currentLevel = client.level;
        if (currentLevel == null || client.player == null) {
            level = null;
            clearRunState();
            return false;
        }

        if (currentLevel != level) {
            clearRunState();
            level = currentLevel;
        }

        if (!SIConfig.General.ENABLE_IMPRINTS.get()) {
            clearRunState();
            return false;
        }

        tickCounter += 1L;
        return true;
    }
}
