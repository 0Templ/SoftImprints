package com.nine.softimprints.client.ui.cache;

import com.nine.softimprints.client.ui.component.preview.brush.BrushHistory;
import com.nine.softimprints.client.ui.component.search.SearchListEntry;
import com.nine.softimprints.client.ui.screen.settings.EditorGroup;
import net.minecraft.resources.Identifier;

public class UICache {


    private static Identifier selectedProfile;

    public static Identifier selectedProfile() {
        return selectedProfile;
    }

    public static void setSelectedProfileId(Identifier id) {
        selectedProfile = id;
    }


    private static int previewResolution = 64;

    public static int previewResolution() {
        return previewResolution;
    }
    public static void setPreviewResolution(int value) {
        previewResolution = value;
    }


    private static double brushSize = 12;

    public static double brushSize() {
        return brushSize;
    }
    public static void setBrushSize(double value) {
        brushSize = value;
    }


    private static boolean debugPreviewMode = false;

    public static boolean debugPreviewMode() {
        return debugPreviewMode;
    }

    public static void setDebugPreviewMode(boolean value) {
        debugPreviewMode = value;
    }



    private static EditorGroup editorGroup = EditorGroup.GENERAL;

    public static EditorGroup editorGroup() {
        return editorGroup;
    }

    public static void setEditorGroup(EditorGroup value) {
        editorGroup = value;
    }



    private static BrushHistory brushHistory = new BrushHistory();

    public static BrushHistory brushHistory() {
        return brushHistory;
    }

    public static void setBrushHistory(BrushHistory value) {
        brushHistory = value;
    }


}
