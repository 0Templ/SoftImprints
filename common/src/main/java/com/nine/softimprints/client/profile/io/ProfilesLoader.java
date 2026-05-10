package com.nine.softimprints.client.profile.io;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.client.platform.Platform;
import com.nine.softimprints.client.profile.ImprintProfiles;
import com.nine.softimprints.client.profile.io.json.JsonProfile;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ProfilesLoader {

    private static final String RESOURCES_FOLDER = "imprint_profiles";

    // Model baker goes on several threads before ResourceLoader's actions
    // So we should be sure that profiles are loaded before Model Baker
    private static final Object LOCK = new Object();

    private static volatile boolean everLoaded = false;

    public static void ensureLoaded(ResourceManager rm) {
        if (everLoaded) return;
        synchronized (LOCK) {
            if (everLoaded) return;
            reloadProfiles(rm);
            everLoaded = true;
        }
    }

    public static void reloadProfiles(ResourceManager rm) {
        synchronized (LOCK) {
            reload(rm);
            everLoaded = true;
        }
    }

    public static void reload(ResourceManager rm) {
        var cfgProfilesDir = configProfilesDir();

        var rawRes = ProfilesExtraction.readRawProfilesFromResources(rm, RESOURCES_FOLDER);
        var rawCfg = ProfilesExtraction.readRawProfilesFromConfig(cfgProfilesDir);

        var builtInJson = ProfileOperations.convertToRawMap(rawRes);
        var overrideJson = ProfileOperations.convertToRawMap(rawCfg);

        for (var id : overrideJson.keySet()) {
            if (!builtInJson.containsKey(id)) {
                SICommon.LOGGER.warn("Override for unknown profile {}, ignored", id);
            }
        }

        Map<Identifier, JsonProfile> mergedJson = new HashMap<>();
        for (var e : builtInJson.entrySet()) {
            JsonProfile base = e.getValue();
            JsonProfile override = overrideJson.get(e.getKey());
            mergedJson.put(e.getKey(), override == null ? base : base.merge(override));
        }

        var domain = ProfileOperations.convertToDomainMap(mergedJson);
        var builtinDomain = ProfileOperations.convertToDomainMap(builtInJson);

        ImprintProfiles.replaceMain(domain);

        ImprintProfiles.replaceBuiltin(builtInJson, builtinDomain);
    }

    public static Path configProfilesDir() {
        return Platform.CORE.getConfigPath().resolve("softimprints").resolve("profiles");
    }



}
