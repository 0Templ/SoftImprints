package com.nine.softimprints.ui.component.list.element;

import com.nine.softimprints.ui.component.list.ConfigListWidget;
import com.nine.softimprints.ui.component.list.ListGroup;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public interface ConfigListEntry {

    int getX();

    int getY();

    int getWidth();

    int getHeight();

    void updateEntryLayout(int x, int y, int width);

    void updateHeight(int height);

    void attachToList(ConfigListWidget list, ListGroup group);

    void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick);

    default void mouseMoved(double x, double y) {
    }

    default boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return false;
    }

    default boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }

    default boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return false;
    }

    default boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return false;
    }

    default boolean keyPressed(KeyEvent event) {
        return false;
    }

    default boolean keyReleased(KeyEvent event) {
        return false;
    }

    default boolean charTyped(CharacterEvent event) {
        return false;
    }

    default ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return null;
    }

    default boolean isFocusable() {
        return true;
    }

    default void setFocused(boolean focused) {
    }

    default boolean isFocused() {
        return false;
    }

    default boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= getX()
                && mouseY >= getY()
                && mouseX < getX() + getWidth()
                && mouseY < getY() + getHeight();
    }

    default boolean fullWidth() {
        return false;
    }
}
