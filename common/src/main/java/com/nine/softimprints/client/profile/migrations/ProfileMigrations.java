package com.nine.softimprints.client.profile.migrations;

import com.google.gson.JsonObject;

import java.util.Map;
import java.util.function.UnaryOperator;

public class ProfileMigrations {


    private static final Map<Integer, UnaryOperator<JsonObject>> MIGRATION_STEPS = Map.of(
            1, ProfileMigrations::v0_to_v1
    );

    public static JsonObject migrate(
            JsonObject json,
            int from,
            int to
    ){
        int prevStep = -1;
        var ret = json.deepCopy();
        for (int v = from; v < to; v++) {
            var step = MIGRATION_STEPS.get(v);
            if (!MIGRATION_STEPS.containsKey(v)) continue;
            ret = step.apply(ret);
            if (ret == null) {
                if (v != prevStep){
                    throw new IllegalArgumentException("No migration step from v" + prevStep + " to v" + v);
                } else {
                    throw new IllegalArgumentException("Couldn't migrate profile to v" + v  );
                }
            }
            prevStep = v;
        }
        return ret;
    }

    private static JsonObject v0_to_v1(JsonObject prev){
        return prev;
    }



}
