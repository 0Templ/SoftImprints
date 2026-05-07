package com.nine.softimprints.client.ui.screen.settings;

import com.nine.softimprints.client.ui.context.EditorContext;
import com.nine.softimprints.client.ui.context.PreviewSettings;

public record GroupBuildContext(
        EditorContext editorContext,
        PreviewSettings previewSettings
) {
}
