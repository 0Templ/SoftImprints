package com.nine.softimprints.client.ui.context;

import com.nine.softimprints.client.ui.cache.UICache;

public class PreviewSettings {

    private int resolution;
    private double brushSize;
    private boolean debugMode;

    public PreviewSettings() {
        this.resolution = UICache.previewResolution();
        this.brushSize = UICache.brushSize();
        this.debugMode = UICache.debugPreviewMode();
    }

    public int resolution() {
        return resolution;
    }

    public void setResolution(int resolution) {
        this.resolution = Math.max(1, resolution);
        UICache.setPreviewResolution(this.resolution);
    }

    public double brushSize() {
        return brushSize;
    }

    public void setBrushSize(double brushSize) {
        this.brushSize = Math.max(1.0D, brushSize);
        UICache.setBrushSize(this.brushSize);
    }

    public boolean debugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        UICache.setDebugPreviewMode(debugMode);
    }

    public void toggleDebugMode() {
        setDebugMode(!debugMode);
    }

}
