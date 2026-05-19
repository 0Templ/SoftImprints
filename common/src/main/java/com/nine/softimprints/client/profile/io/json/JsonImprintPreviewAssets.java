package com.nine.softimprints.client.profile.io.json;

import com.google.gson.annotations.SerializedName;

public record JsonImprintPreviewAssets(
        @SerializedName("base")
        String base,
        @SerializedName("icon")
        String icon
)
{
}
