package com.nine.softimprints.model;

import com.google.gson.annotations.SerializedName;

public enum SurfaceMode {

    OVERLAY,
    @SerializedName(value = "repaint", alternate = {"TOP", "top"})
    REPAINT

    ;

}
