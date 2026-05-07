package com.nine.softimprints.client.ui.component.list.element.row;

import com.nine.softimprints.client.ui.component.list.element.CompositeListEntry;
import net.minecraft.client.gui.components.AbstractWidget;

import java.util.List;

public final class RowListEntry extends CompositeListEntry {

    private final int gap;
    private final List<RowItem> items;
    private final int totalWeight;
    private final int totalFixedWidth;
    private final int weightedItemCount;

    public RowListEntry(int height, int gap, List<RowItem> items) {
        super(height);
        this.gap = gap;
        this.items = List.copyOf(items);
        this.totalWeight = this.items.stream().mapToInt(RowItem::weight).sum();
        this.totalFixedWidth = this.items.stream().mapToInt(RowItem::fixedWidth).sum();
        this.weightedItemCount = (int) this.items.stream().filter(item -> item.weight() > 0).count();

        for (RowItem item : this.items) {
            if (item instanceof WeightedWidget weightedWidget) {
                addWidget(weightedWidget.widget());
            }
            if (item instanceof FixedWidget fixedWidget) {
                addWidget(fixedWidget.widget());
            }
        }
    }

    @Override
    public void updateEntryLayout(int x, int y, int width) {
        super.updateEntryLayout(x, y, width);

        int gapsWidth = Math.max(0, (items.size() - 1) * gap);
        int weightedWidth = Math.max(0, width - gapsWidth - totalFixedWidth);

        int currentX = getX();
        int usedWeightedWidth = 0;
        int weightedSeen = 0;

        for (int i = 0; i < items.size(); i++) {
            RowItem item = items.get(i);
            int w = item.fixedWidth();
            if (item.weight() > 0) {
                w = (weightedSeen == weightedItemCount - 1)
                        ? weightedWidth - usedWeightedWidth
                        : weightedWidth * item.weight() / totalWeight;
                weightedSeen++;
                usedWeightedWidth += w;
            }

            switch (item) {
                case WeightedWidget weightedWidget -> place(weightedWidget.widget(), currentX, w);
                case FixedWidget fixedWidget -> place(fixedWidget.widget(), currentX, w);
                case WeightedSpace ignored -> {
                }
                case FixedSpace ignored -> {
                }
            }

            currentX += w + gap;
        }
    }

    private void place(AbstractWidget widget, int x, int width) {
        widget.setX(x);
        widget.setY(getY());
        widget.setWidth(Math.max(0, width));
        widget.setHeight(getHeight());
    }
}
