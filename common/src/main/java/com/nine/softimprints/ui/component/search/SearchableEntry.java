package com.nine.softimprints.ui.component.search;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface SearchableEntry<T> {

    T value();

    boolean matches(String lowercaseQuery);

    void render(
            GuiGraphicsExtractor graphics,
            int x, int y, int width, int height,
            int mouseX, int mouseY, float partialTick,
            boolean hovered, boolean selected
    );
}
