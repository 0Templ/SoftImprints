package com.nine.softimprints.profile.migrations;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.nine.softimprints.profile.io.json.JsonProfile;

import java.util.Map;
import java.util.function.UnaryOperator;

public class ProfileMigrations {


    private static final Map<Integer, UnaryOperator<JsonObject>> MIGRATION_STEPS = Map.of(
            1, ProfileMigrations::v0_to_v1,
            2, ProfileMigrations::v1_to_v2,
            3, ProfileMigrations::v2_to_v3
    );

    public static JsonObject migrate(
            JsonObject json,
            int from,
            int to
    ) {
        int prevStep = -1;
        var ret = json.deepCopy();
        for (int v = from + 1; v <= to; v++) {
            var step = MIGRATION_STEPS.get(v);
            if (!MIGRATION_STEPS.containsKey(v)) continue;
            ret = step.apply(ret);
            if (ret == null) {
                if (v != prevStep) {
                    throw new IllegalArgumentException("No migration step from v" + prevStep + " to v" + v);
                } else {
                    throw new IllegalArgumentException("Couldn't migrate profile to v" + v);
                }
            }
            prevStep = v;
        }

        return ret;
    }

    private static JsonObject v2_to_v3(JsonObject prev) {
        JsonObject next = prev.deepCopy();
        JsonElement priority = next.get("priority");
        if (priority != null && priority.isJsonPrimitive()
                && priority.getAsJsonPrimitive().isNumber()
                && priority.getAsInt() == 0) {
            next.remove("priority");
        }
        next.addProperty(JsonProfile.SCHEMA_KEY, 3);
        return next;
    }

    private static JsonObject v1_to_v2(JsonObject prev) {
        JsonObject next = prev.deepCopy();

        rename(next, "supportedBlocks", "supported_blocks");
        rename(next, "textureSets", "texture_sets");

        JsonObject textureSets = object(next, "texture_sets");
        if (textureSets != null) {
            rename(textureSets, "texturesByValue", "textures_by_value");
            rename(textureSets, "initLayer", "zero_layer");
        }

        JsonArray layers = array(next, "layers");
        if (layers != null) {
            for (JsonElement element : layers) {
                if (!element.isJsonObject()) continue;
                JsonObject layer = element.getAsJsonObject();

                rename(layer, "innerJitter", "inner_jitter");
                rename(layer, "outerJitter", "outer_jitter");
            }
        }

        if (!next.has("resolution")) {
            JsonObject resolution = new JsonObject();
            resolution.addProperty("map_size", 16);
            next.add("resolution", resolution);
        }

        next.addProperty(JsonProfile.SCHEMA_KEY, 2);

        return next;
    }


    private static JsonObject v0_to_v1(JsonObject prev) {
        return prev;
    }


    private static void rename(
            JsonObject obj,
            String from,
            String to
    ) {
        if (!obj.has(from)) return;
        if (obj.has(to)) return;
        obj.add(to, obj.remove(from));
    }

    private static JsonObject object(
            JsonObject obj,
            String key
    ) {
        JsonElement element = obj.get(key);
        return element != null && element.isJsonObject() ? element.getAsJsonObject() : null;
    }

    private static JsonArray array(
            JsonObject obj,
            String key
    ) {
        JsonElement element = obj.get(key);
        return element != null && element.isJsonArray() ? element.getAsJsonArray() : null;
    }

}
