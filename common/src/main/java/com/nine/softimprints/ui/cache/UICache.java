package com.nine.softimprints.ui.cache;

import com.nine.softimprints.ui.component.preview.brush.BrushHistory;
import com.nine.softimprints.ui.screen.settings.EditorGroup;
import net.minecraft.resources.Identifier;

public class UICache {


    private static Identifier selectedProfile;
    private static int previewResolution = 64;
    private static double brushSize = 12;
    private static boolean debugPreviewMode = false;
    private static boolean priorityEditMode = false;
    private static EditorGroup editorGroup = EditorGroup.GENERAL;
    private static BrushHistory brushHistory = new BrushHistory();

    public static Identifier selectedProfile() {
        return selectedProfile;
    }

    public static void setSelectedProfileId(Identifier id) {
        selectedProfile = id;
    }

    public static int previewResolution() {
        return previewResolution;
    }

    public static void setPreviewResolution(int value) {
        previewResolution = value;
    }

    public static double brushSize() {
        return brushSize;
    }

    public static void setBrushSize(double value) {
        brushSize = value;
    }

    public static boolean priorityEditMode() {
        return priorityEditMode;
    }

    public static void setPriorityEditMode(boolean value) {
        priorityEditMode = value;
    }

    public static boolean debugPreviewMode() {
        return debugPreviewMode;
    }

    public static void setDebugPreviewMode(boolean value) {
        debugPreviewMode = value;
    }

    public static EditorGroup editorGroup() {
        return editorGroup;
    }

    public static void setEditorGroup(EditorGroup value) {
        editorGroup = value;
    }

    public static BrushHistory brushHistory() {
        return brushHistory;
    }

    public static void setBrushHistory(BrushHistory value) {
        brushHistory = value;
    }


}
