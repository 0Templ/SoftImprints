package com.nine.softimprints.ui.component.group;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;

public interface GroupEntry<T> {

    int width();

    boolean interactive();

    GroupEntry<T> withWidth(int width);

    default boolean fixedWidth() {
        return false;
    }

    void render(
            GuiGraphicsExtractor graphics,
            AbstractWidget owner,
            int x,
            int y,
            int height,
            boolean active,
            boolean hovered,
            GroupMarker marker
    );

    default int preferredWidth() {
        return this.width();
    }

    default T value() {
        throw new IllegalStateException("Entry is not selectable");
    }

}
