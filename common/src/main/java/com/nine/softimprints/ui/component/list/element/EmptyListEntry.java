package com.nine.softimprints.ui.component.list.element;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class EmptyListEntry extends AbstractConfigListEntry {

    public EmptyListEntry(int height) {
        super(height);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
    }
}
