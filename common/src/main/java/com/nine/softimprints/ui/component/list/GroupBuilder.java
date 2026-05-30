package com.nine.softimprints.ui.component.list;

import com.nine.softimprints.profile.catalog.entry.InvalidProfileEntry;
import com.nine.softimprints.ui.component.list.element.*;
import com.nine.softimprints.ui.component.list.element.row.RowItem;
import com.nine.softimprints.ui.component.list.element.row.RowListEntry;
import com.nine.softimprints.ui.component.search.SearchListEntry;
import com.nine.softimprints.ui.component.widget.LabelWidget;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

import java.net.URI;
import java.util.ArrayList;
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

    public GroupBuilder issueDetails(InvalidProfileEntry invalidEntry) {
        var issue = invalidEntry.issue();
        var ret = label(issue.title());
        for (var detail : issue.details()){
            label(detail);
        }
        label(Component.translatable("imprint_profile.issue.source", invalidEntry.source().path()));
        return ret;
    }

    public GroupBuilder label(Component text) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text));
    }

    public GroupBuilder label(Component text, Runnable onClickAction) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, onClickAction));
    }

    public GroupBuilder label(Component text, int textColor, int textColorHovered) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, textColor, textColorHovered));
    }

    public GroupBuilder label(Component text, Component tooltip) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, tooltip));
    }

    public GroupBuilder label(Component text, Component tooltip, Runnable onClickAction) {
        return add(new LabelEntry(DEFAULT_LABEL_GAP, text, tooltip, onClickAction));
    }

    public GroupBuilder singleLineLabel(Component text) {
        return add(LabelEntry.singleLine(DEFAULT_LABEL_GAP, text));
    }

    public GroupBuilder singleLineLabel(Component text, Runnable onClickAction) {
        return add(LabelEntry.singleLine(DEFAULT_LABEL_GAP, text, null, onClickAction));
    }

    public GroupBuilder singleLineLabel(Component text, Component tooltip) {
        return add(LabelEntry.singleLine(DEFAULT_LABEL_GAP, text, tooltip));
    }

    public GroupBuilder singleLineLabel(Component text, Component tooltip, Runnable onClickAction) {
        return add(LabelEntry.singleLine(DEFAULT_LABEL_GAP, text, tooltip, onClickAction));
    }

    public GroupBuilder linkLabel(Component text, URI uri) {
        return label(text, LabelEntry.openLinkAction(uri));
    }

    public GroupBuilder linkLabel(Component text, Component tooltip, URI uri) {
        return label(text, tooltip, LabelEntry.openLinkAction(uri));
    }

    public GroupBuilder singleLineLinkLabel(Component text, URI uri) {
        return singleLineLabel(text, LabelEntry.openLinkAction(uri));
    }

    public GroupBuilder singleLineLinkLabel(Component text, Component tooltip, URI uri) {
        return singleLineLabel(text, tooltip, LabelEntry.openLinkAction(uri));
    }

    public GroupBuilder spacer(int height) {
        return add(new EmptyListEntry(height));
    }

    public GroupBuilder separator() {
        return add(new SeparatorListEntry(null));
    }

    public GroupBuilder widget(AbstractWidget widget) {
        return add(new ConfigWidgetEntry(widget));
    }

    public GroupBuilder row(RowItem... items) {
        return add(new RowListEntry(rowHeight, ROW_INNER_GAP, List.of(items)));
    }

    public GroupBuilder rowLabels(Font font, LabelWidget... labels) {

        List<RowItem> items = new ArrayList<>();
        items.add(RowItem.space(1));
        for (LabelWidget label : labels) {
            items.add(RowItem.fixedWidget(font.width(label.getMessage()), label));
        }
        items.add(RowItem.space(1));
        return add(new RowListEntry(rowHeight, ROW_INNER_GAP, items));
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
