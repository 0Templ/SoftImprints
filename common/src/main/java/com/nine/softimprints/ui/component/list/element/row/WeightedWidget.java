package com.nine.softimprints.ui.component.list.element.row;

import net.minecraft.client.gui.components.AbstractWidget;

public record WeightedWidget(int weight, AbstractWidget widget) implements RowItem {
}
