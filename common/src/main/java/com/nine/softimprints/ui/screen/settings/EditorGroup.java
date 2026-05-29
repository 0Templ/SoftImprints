package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.api.meta.update.SIUpdateService;
import com.nine.softimprints.ui.component.group.GroupMarker;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum EditorGroup {

    LAYERS("config.softimprints.group.layers.title"),
    BLOCKS("config.softimprints.group.blocks.title"),
    TARGETS("config.softimprints.group.targets.title"),
    GENERAL("config.softimprints.group.general.title"),
    STORAGE("config.softimprints.group.storage.title"),
    PLUGINS("config.softimprints.group.plugins.title"),
    INFO("config.softimprints.group.info.title", 21,
            () -> SIUpdateService.hasUpdate() ? GroupMarker.UPDATE : null),
    ;

    private final String translationKey;
    private final int fixedSwitcherWidth;
    private final Supplier<GroupMarker> markerProvider;

    EditorGroup(String translationKey) {
        this(translationKey, 0);
    }

    EditorGroup(String translationKey, int fixedSwitcherWidth) {
        this(translationKey, fixedSwitcherWidth, () -> null);
    }

    EditorGroup(String translationKey, int fixedSwitcherWidth, Supplier<GroupMarker> markerProvider) {
        this.translationKey = translationKey;
        this.fixedSwitcherWidth = fixedSwitcherWidth;
        this.markerProvider = markerProvider;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }

    public int fixedSwitcherWidth() {
        return fixedSwitcherWidth;
    }

    @Nullable
    public GroupMarker marker() {
        return markerProvider.get();
    }
}
