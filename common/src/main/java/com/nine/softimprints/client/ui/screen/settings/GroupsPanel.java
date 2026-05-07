package com.nine.softimprints.client.ui.screen.settings;

import com.nine.softimprints.client.ui.component.list.ConfigListWidget;
import com.nine.softimprints.client.ui.component.list.ListGroup;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroupsPanel {

    private final ConfigListWidget list;
    private final GroupBuildContext context;
    private final List<SettingsGroupFactory> factories;
    private final Map<EditorGroup, ListGroup> staticGroups = new LinkedHashMap<>();

    private EditorGroup selected;

    public GroupsPanel(
            ConfigListWidget list,
            GroupBuildContext context,
            List<SettingsGroupFactory> factories
    ) {
        this.list = list;
        this.context = context;
        this.factories = List.copyOf(factories);
        this.selected = this.factories.isEmpty() ? null : this.factories.getFirst().key();
    }

    public EditorGroup selected() {
        return selected;
    }

    public void rebuild() {
        List<ListGroup> groups = factories.stream()
                .map(this::resolveGroup)
                .collect(Collectors.toList());
        list.setGroups(groups, selectedIndex());
    }

    /** Rebuilds all groups, discarding any cached static groups. */
    public void rebuildFull() {
        staticGroups.clear();
        rebuild();
    }

    public void select(EditorGroup key) {
        if (!contains(key)) {
            return;
        }
        this.selected = key;
        int index = selectedIndex();
        if (index >= 0) {
            list.setActiveGroup(index);
        }
    }

    public void onProfileChanged() {
        boolean hasDynamicGroups = factories.stream().anyMatch(SettingsGroupFactory::rebuildOnProfileChange);
        if (hasDynamicGroups) {
            rebuild();
        }
    }

    private ListGroup resolveGroup(SettingsGroupFactory factory) {
        if (factory.rebuildOnProfileChange()) {
            return factory.build(context);
        }
        return staticGroups.computeIfAbsent(factory.key(), _ -> factory.build(context));
    }

    private int selectedIndex() {
        for (int i = 0; i < factories.size(); i++) {
            if (factories.get(i).key() == selected) {
                return i;
            }
        }
        if (factories.isEmpty()) {
            selected = null;
            return -1;
        }
        selected = factories.getFirst().key();
        return 0;
    }

    private boolean contains(EditorGroup key) {
        for (SettingsGroupFactory factory : factories) {
            if (factory.key() == key) {
                return true;
            }
        }
        return false;
    }

}
