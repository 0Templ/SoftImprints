package com.nine.softimprints.model;

import com.google.gson.annotations.SerializedName;

public enum SurfaceMode {

    @SerializedName(value = "overlay", alternate = {"OVERLAY"})
    OVERLAY,
    @SerializedName(value = "repaint", alternate = {"TOP", "top"})
    REPAINT;

}
