package com.nine.softimprints.client.ui.component.list;

import com.nine.softimprints.client.ui.component.list.element.ConfigListEntry;
import com.nine.softimprints.client.ui.component.list.element.ConfigWidgetEntry;
import com.nine.softimprints.client.ui.component.list.element.EmptyListEntry;
import com.nine.softimprints.client.ui.component.list.element.LabelEntry;
import com.nine.softimprints.client.ui.component.list.element.row.RowItem;
import com.nine.softimprints.client.ui.component.list.element.row.RowListEntry;
import com.nine.softimprints.client.ui.component.search.SearchListEntry;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;

public final class GroupBuilder {

    private static final int DEFAULT_ROW_HEIGHT = 20;
    private static final int DEFAULT_LABEL_GAP = 2;
    private static final int ROW_INNER_GAP = 2;

    private final ListGroup group;
    private int rowHeight = DEFAULT_ROW_HEIGHT;
    private ConfigListEntry lastEntry;

    private GroupBuilder(ListGroup group) {
        this.group = group;
    }

    public static GroupBuilder of(Component label) {
        return new GroupBuilder(new ListGroup(label));
    }

    public GroupBuilder rowHeight(int height) {
        this.rowHeight = height;
        return this;
    }

    public GroupBuilder height(int height) {
        if (lastEntry == null) {
            throw new IllegalStateException("height() called before any entry was added");
        }
        lastEntry.updateHeight(height);
        return this;
    }

    public GroupBuilder section(Component text) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text));
    }

    public GroupBuilder section(Component text, Component tooltip) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, tooltip));
    }

    public GroupBuilder sectionMarker(
            Component text,
            Component tooltip,
            Component marker,
            Component markerTooltip,
            BooleanSupplier markerVisible
    ) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, tooltip, marker, markerTooltip, markerVisible));
    }

    public GroupBuilder label(Component text) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text));
    }

    public GroupBuilder label(Component text, Component tooltip) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, tooltip));
    }

    public GroupBuilder spacer(int height) {
        return add(new EmptyListEntry(height));
    }

    public GroupBuilder widget(AbstractWidget widget) {
        return add(new ConfigWidgetEntry(widget));
    }

    public GroupBuilder row(RowItem... items) {
        return add(new RowListEntry(rowHeight, ROW_INNER_GAP, List.of(items)));
    }

    public GroupBuilder rowWidgets(AbstractWidget... widgets) {
        RowItem[] items = Arrays.stream(widgets)
                .map(widget -> RowItem.widget(1, widget))
                .toArray(RowItem[]::new);
        return row(items);
    }

    public GroupBuilder customRow(ConfigListEntry entry) {
        return add(entry);
    }

    public GroupBuilder searchList(SearchListEntry<?> searchList) {
        return add(searchList);
    }

    public ListGroup build() {
        return group;
    }

    private GroupBuilder add(ConfigListEntry entry) {
        group.addEntry(entry);
        lastEntry = entry;
        return this;
    }
}
