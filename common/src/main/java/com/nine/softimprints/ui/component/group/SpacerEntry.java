package com.nine.softimprints.ui.component.group;

import com.nine.softimprints.ui.util.region.ChromeRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

public record SpacerEntry<T>(int width) implements GroupEntry<T> {

    @Override
    public boolean interactive() {
        return false;
    }

    @Override
    public GroupEntry<T> withWidth(int width) {
        return new SpacerEntry<>(width);
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            AbstractWidget owner,
            int x,
            int y,
            int height,
            boolean active,
            boolean hovered,
            GroupMarker marker
    ) {
        ChromeRenderer.footerLine(graphics, x, y + height - 2, width);
    }

}
