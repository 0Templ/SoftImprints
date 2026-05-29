package com.nine.softimprints.ui.layout;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.Objects;
import java.util.function.Consumer;

public final class WidgetElement implements UILayoutElement {

    private final AbstractWidget widget;

    public WidgetElement(AbstractWidget widget) {
        this.widget = Objects.requireNonNull(widget, "widget");
    }

    public AbstractWidget widget() {
        return widget;
    }

    @Override
    public void place(LayoutRect bounds) {
        widget.setX(bounds.x());
        widget.setY(bounds.y());
        widget.setSize(bounds.width(), bounds.height());
    }

    @Override
    public void addWidgets(Consumer<AbstractWidget> add) {
        add.accept(widget);
    }
}
