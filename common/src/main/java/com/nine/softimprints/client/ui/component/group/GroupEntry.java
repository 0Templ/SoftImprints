package com.nine.softimprints.client.ui.component.group;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

public interface GroupEntry<T> {

    int width();

    boolean interactive();

    GroupEntry<T> withWidth(int width);

    void render(GuiGraphicsExtractor graphics, AbstractWidget owner, int x, int y, int height, boolean active, boolean hovered);

    default int preferredWidth() {
        return this.width();
    }

    default T value() {
        throw new IllegalStateException("Entry is not selectable");
    }

    static <T> OptionEntry<T> option(T value, Component label) {
        return new OptionEntry<>(value, Minecraft.getInstance().font.width(label), label);
    }

}
