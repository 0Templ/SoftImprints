package com.nine.softimprints.client.ui.screen.settings;

import net.minecraft.network.chat.Component;

public enum EditorGroup {

    LAYERS("config.softimprints.group.layers.title"),
    BLOCKS("config.softimprints.group.blocks.title"),
    TARGETS("config.softimprints.group.targets.title"),
    GENERAL("config.softimprints.group.general.title"),
    STORAGE("config.softimprints.group.storage.title"),
    PLUGINS("config.softimprints.group.plugins.title"),

    ;

    private final String translationKey;

    EditorGroup(String translationKey) {
        this.translationKey = translationKey;
    }

    public Component label() {
        return Component.translatable(translationKey);
    }
}
