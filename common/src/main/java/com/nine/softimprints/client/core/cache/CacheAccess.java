package com.nine.softimprints.client.core.cache;

import net.minecraft.client.multiplayer.ClientLevel;

public final class CacheAccess {

    private static volatile LevelData currentCache;
    private static volatile ClientLevel currentLevel;

    public static LevelData current(){
        return currentCache;
    }

    public static boolean isCurrentLevel(ClientLevel level) {
        return currentLevel == level && (level == null || currentCache != null);
    }

    public static void onLevelSwitch(ClientLevel level) {
        LevelData nextCache = (level == null) ? null : new LevelData();
        currentCache = nextCache;
        currentLevel = level;
    }

}
