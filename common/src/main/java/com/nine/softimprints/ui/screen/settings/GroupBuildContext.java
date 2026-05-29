package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.ui.context.EditorContext;
import com.nine.softimprints.ui.context.PreviewSettings;

public record GroupBuildContext(
        EditorContext editorContext,
        PreviewSettings previewSettings
) {
}
