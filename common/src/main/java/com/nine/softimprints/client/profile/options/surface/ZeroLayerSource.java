package com.nine.softimprints.client.profile.options.surface;

import com.google.gson.annotations.SerializedName;

public enum ZeroLayerSource {

    @SerializedName(value = "surface", alternate = {"SURFACE"})
    SURFACE,
    @SerializedName(value = "profile", alternate = {"PROFILE"})
    PROFILE

}
