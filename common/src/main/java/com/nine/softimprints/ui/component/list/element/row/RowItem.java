package com.nine.softimprints.ui.component.list.element.row;

import net.minecraft.client.gui.components.AbstractWidget;

public sealed interface RowItem permits WeightedWidget, WeightedSpace, FixedWidget, FixedSpace {

    static RowItem widget(
            int weight,
            AbstractWidget widget
    ) {
        return new WeightedWidget(weight, widget);
    }

    static RowItem fixedWidget(
            int width,
            AbstractWidget widget
    ) {
        return new FixedWidget(width, widget);
    }

    static RowItem space(int weight) {
        return new WeightedSpace(weight);
    }

    static RowItem fixedSpace(int width) {
        return new FixedSpace(width);
    }

    default int weight() {
        return 0;
    }

    default int fixedWidth() {
        return 0;
    }
}
