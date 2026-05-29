package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.ui.component.list.ListGroup;
import net.minecraft.network.chat.Component;

public interface SettingsGroupFactory {

    EditorGroup key();

    GroupZone zone();

    default Component label() {
        return key().label();
    }

    boolean rebuildOnProfileChange();

    ListGroup build(GroupBuildContext context);

}
