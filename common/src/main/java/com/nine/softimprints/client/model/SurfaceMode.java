package com.nine.softimprints.client.model;

import com.google.gson.annotations.SerializedName;

public enum SurfaceMode {

    OVERLAY,
    @SerializedName(value = "repaint", alternate = {"TOP", "top"})
    REPAINT

    ;

}
