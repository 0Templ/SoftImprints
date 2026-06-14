package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.ui.cache.UICache;
import com.nine.softimprints.ui.component.group.GroupSwitcher;

import java.util.LinkedHashMap;
import java.util.Map;

public class GroupNavigation {

    private final GroupsPanel panel;
    private final Map<GroupZone, GroupSwitcher<EditorGroup>> switchers = new LinkedHashMap<>();

    public GroupNavigation(GroupsPanel panel) {
        this.panel = panel;
    }

    public void register(
            GroupZone zone,
            GroupSwitcher<EditorGroup> switcher
    ) {
        switchers.put(zone, switcher);
        syncSwitchers();
    }

    public void select(EditorGroup key) {
        panel.select(key);
        syncSwitchers();
        UICache.setEditorGroup(key);
    }

    public void onProfileChanged() {
        panel.onProfileChanged();
        syncSwitchers();
    }

    public void syncSwitchers() {
        EditorGroup selected = panel.selected();
        switchers.values().forEach(switcher -> switcher.setActiveValue(selected));
    }

}
