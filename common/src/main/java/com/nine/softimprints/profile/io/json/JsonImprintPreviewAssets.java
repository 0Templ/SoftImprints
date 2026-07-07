package com.nine.softimprints.profile.io.json;

import com.google.gson.annotations.SerializedName;

import javax.annotation.Nullable;

public record JsonImprintPreviewAssets(
        @SerializedName("base")
        String base,
        @SerializedName("icon")
        String icon,
        @Nullable
        @SerializedName("landing_sound")
        String landingSound
) {
}
