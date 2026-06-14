package com.nine.softimprints.profile.io;

import com.nine.softimprints.SICommon;
import com.nine.softimprints.platform.Platform;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.catalog.entry.ImprintProfileEntry;
import com.nine.softimprints.profile.catalog.entry.ValidProfileEntry;
import com.nine.softimprints.profile.io.json.JsonProfile;
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

        var rawRes = ProfilesReader.readRawProfilesFromResources(rm, RESOURCES_FOLDER);
        var rawCfg = ProfilesReader.readRawProfilesFromConfig(cfgProfilesDir);

        var overrideJson = ProfileOperations.convertToRawMap(rawCfg);

        // 1.
        var entriesResult = ProfileOperations.convertToEntries(rawRes);

        // 2. To fill
        Map<Identifier, JsonProfile> builtinJson = new HashMap<>();
        Map<Identifier, ImprintProfile> builtinReady = new HashMap<>();

        Map<Identifier, ImprintProfile> ready = new HashMap<>();

        // 3. Filling
        var raws = entriesResult.raw();
        var entries = entriesResult.entries();
        for (var entry : entries.entrySet()) {
            Identifier id = entry.getKey();
            ImprintProfileEntry profileEntry = entry.getValue();
            if (profileEntry instanceof ValidProfileEntry valid) {
                var profile = valid.profile();
                ready.put(id, profile);
                builtinReady.put(id, profile);

                var raw = raws.get(id);
                builtinJson.put(id, raw);
                if (overrideJson.containsKey(id)) {
                    JsonProfile override = overrideJson.get(id);
                    try {
                        JsonProfile merged = raw.merge(override);
                        ready.put(id, ProfileOperations.parseFromRawToDomain(id, merged));
                    } catch (RuntimeException e) {
                        if (Platform.CORE.inDevEnvironment())
                            SICommon.LOGGER.warn("Couldn't apply profile override {}: {}", id, e.getMessage());
                        // Erase them?
                    }
                }
            }
        }

        ImprintProfiles.replaceMain(ready);
        ImprintProfiles.replaceBuiltin(builtinJson, builtinReady);
        ImprintProfiles.replaceEntries(entries);
    }

    public static Path configProfilesDir() {
        return Platform.CORE.getConfigPath().resolve("softimprints").resolve("profiles");
    }


}
