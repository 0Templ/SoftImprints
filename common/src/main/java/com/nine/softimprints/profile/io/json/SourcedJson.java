package com.nine.softimprints.profile.io.json;

import com.google.gson.JsonElement;

public record SourcedJson(
        JsonElement json,
        JsonSource source
) {}