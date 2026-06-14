package com.nine.softimprints.profile.io;

import com.google.gson.JsonElement;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.io.json.JsonProfile;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;


public class ProfilesSaver {

    public static void save(
            Identifier id,
            ImprintProfile draft
    ) {
        JsonProfile builtinJson = ImprintProfiles.getBuiltInJson(id);
        if (builtinJson == null) {
            SICommon.LOGGER.error("Cannot save override for unknown profile {}", id);
            return;
        }

        JsonProfile draftJson = ProfileOperations.toJsonModel(draft);
        JsonProfile diff = draftJson.nullifyAgainst(builtinJson);

        Path file = ProfilePaths.toConfigPath(ProfilesLoader.configProfilesDir(), id);

        try {
            if (diff.isEmpty()) {
                ProfilesWriter.delete(file);
            } else {
                JsonElement tree = ProfileOperations.toJsonElement(diff);
                ProfilesWriter.write(file, tree);
            }
        } catch (IOException e) {
            SICommon.LOGGER.error("Failed to save profile {}: {}", id, e.getMessage());
            return;
        }

        Map<Identifier, ImprintProfile> current = ImprintProfiles.snapshot();
        Map<Identifier, ImprintProfile> next = new HashMap<>(current);
        next.put(id, draft);
        ImprintProfiles.replaceMain(next);
    }

}
