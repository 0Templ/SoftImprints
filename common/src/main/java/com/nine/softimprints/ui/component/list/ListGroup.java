package com.nine.softimprints.ui.component.list;

import com.nine.softimprints.ui.component.list.element.ConfigListEntry;
import com.nine.softimprints.ui.component.list.element.ConfigWidgetEntry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ListGroup {

    private final Component label;
    private final List<ConfigListEntry> entries = new ArrayList<>();
    private ConfigListWidget list;

    public ListGroup(Component label) {
        this.label = label;
    }

    public Component label() {
        return this.label;
    }

    public List<ConfigListEntry> entries() {
        return Collections.unmodifiableList(this.entries);
    }

    public void addEntry(ConfigListEntry entry) {
        this.entries.add(entry);
        if (this.list != null) {
            entry.attachToList(this.list, this);
            this.list.invalidateLayout();
        }
    }

    public void addWidget(AbstractWidget widget) {
        this.addEntry(new ConfigWidgetEntry(widget));
    }

    public void attachToList(ConfigListWidget list) {
        if (this.list == list) {
            return;
        }
        if (this.list != null) {
            throw new IllegalStateException("Group already attached to list");
        }
        this.list = list;
        for (ConfigListEntry entry : this.entries) {
            entry.attachToList(list, this);
        }
    }
}
