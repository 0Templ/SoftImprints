package com.nine.softimprints.ui.component.list.element.row;

import net.minecraft.client.gui.components.AbstractWidget;

public record FixedWidget(int fixedWidth, AbstractWidget widget) implements RowItem {
}
