package com.nine.softimprints.ui.layout;

import net.minecraft.client.gui.components.AbstractWidget;

import java.util.function.Consumer;

public interface UILayoutElement {

    void place(LayoutRect bounds);

    default void addWidgets(Consumer<AbstractWidget> add) {
    }
}
