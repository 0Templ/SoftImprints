package com.nine.softimprints;

import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.cache.CacheAccess;
import com.nine.softimprints.core.cache.LevelData;
import com.nine.softimprints.core.tick.ImprintTickHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

public class SILifecycle {

    public static void onClientTick(Minecraft client) {
        syncClientLevel(client);
        ImprintTickHandler.tick(client);
    }

    public static void syncLevelCache(ClientLevel level) {
        CacheAccess.onLevelSwitch(level);
    }

    public static void onClientStarted(Minecraft client) {
        syncClientLevel(client);
    }

    public static void onJoin(/*ClientPacketListener handler, */Minecraft client) {
        syncClientLevel(client);
    }

    public static void onDisconnect(/*ClientPacketListener handler, */Minecraft client) {
        syncLevelCache(null);
    }

    public static void onChunkUnload(ChunkAccess chunk) {
        if (!SIConfig.Performance.CLEAR_IMPRINTS_ON_CHUNK_UNLOAD.get()) return;
        LevelData cache = CacheAccess.current();
        if (cache != null) {
            cache.clearAtChunk(chunk.getPos().x(), chunk.getPos().z());
        }
    }

    private static void syncClientLevel(Minecraft client) {
        ClientLevel level = client == null ? null : client.level;
        if (!CacheAccess.isCurrentLevel(level)) {
            syncLevelCache(level);
        }
    }


}
