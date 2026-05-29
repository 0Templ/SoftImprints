package com.nine.softimprints.profile.io;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.profile.io.json.JsonSource;
import com.nine.softimprints.profile.io.json.SourcedJson;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ProfilesReader {

    public static Map<Identifier, SourcedJson> readRawProfilesFromResources(ResourceManager manager, String path){
        var rawRaw = manager.listResources(path, id -> id.getPath().endsWith(".json"));
        Map<Identifier, SourcedJson> ret = new HashMap<>();
        for (var entry : rawRaw.entrySet()){
            var rawId = entry.getKey();
            Identifier normalizedId;
            try {
                var fullPath = rawId.getPath();
                String base = fullPath.substring(
                        path.length() + 1,
                        fullPath.length() - ".json".length());
                normalizedId = Identifier.fromNamespaceAndPath(rawId.getNamespace(), base);
            } catch (Exception e) {
                SICommon.LOGGER.error("Couldn't parse imprint profile: {}", rawId);
                continue;
            }
            try (Reader reader = entry.getValue().openAsReader()) {
                var el = JsonParser.parseReader(reader);
                if (ret.containsKey(normalizedId)){
                    SICommon.LOGGER.warn("Profile with id {} already exists, skipping", rawId);
                }
                else ret.put(normalizedId, new SourcedJson(el, new JsonSource(rawId.toString())));
            }
            catch (Exception e) {
                SICommon.LOGGER.error("Failed to read imprint priority {}: {}", rawId, e.getMessage());
            }
        }
        return ret;
    }

    public static Map<Identifier, JsonElement> readRawProfilesFromConfig(Path path) {
        if (!Files.isDirectory(path)) return Map.of();
        try (var stream = Files.walk(path)) {
            Map<Identifier, JsonElement> ret = new HashMap<>();
            stream.filter(Files::isRegularFile).filter(f -> f.getFileName().toString().endsWith(".json"))
                    .forEach(file -> {
                        Path relative = path.relativize(file);
                        if (relative.getNameCount() < 2){
                            SICommon.LOGGER.warn("Profile config file {} must be inside a namespace folder " +
                                    "(e.g. <namespace>/<name>.json), skipping", relative);
                            return;
                        }
                        try {
                            String nameSpace = relative.getName(0).toString();
                            String base = relative
                                    .subpath(1, relative.getNameCount()).toString()
                                    .replace('\\', '/');
                            base = base.substring(0, base.length() - ".json".length());
                            var id = Identifier.fromNamespaceAndPath(nameSpace, base);
                            try (var reader = Files.newBufferedReader(file)) {
                                var parsed = JsonParser.parseReader(reader);
                                ret.put(id, parsed);
                            } catch (Exception e) {
                                SICommon.LOGGER.error("Failed to read config for imprint profile {}: {}", id, e.getMessage());
                            }
                        } catch (Exception e) {
                            SICommon.LOGGER.error("Skipping invalid imprint profile config {}: {}", file, e.getMessage());
                        }
                    });
            return ret;
        } catch (Exception e) {
            SICommon.LOGGER.error("Failed to read profiles dir {}", e.getMessage());
            return Map.of();
        }
    }


}
