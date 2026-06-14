package com.nine.softimprints.ui.component.list.element;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public final class ConfigWidgetEntry extends AbstractConfigListEntry {

    private final AbstractWidget widget;

    public ConfigWidgetEntry(AbstractWidget widget) {
        super(widget.getHeight());
        this.widget = widget;
    }

    public AbstractWidget widget() {
        return widget;
    }

    @Override
    public void updateEntryLayout(
            int x,
            int y,
            int width
    ) {
        super.updateEntryLayout(x, y, width);
        this.widget.setX(x);
        this.widget.setY(y);
        this.widget.setWidth(width);
        this.widget.setHeight(getHeight());
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        this.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void mouseMoved(
            double x,
            double y
    ) {
        this.widget.mouseMoved(x, y);
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        return this.widget.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return this.widget.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        return this.widget.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double scrollX,
            double scrollY
    ) {
        return this.widget.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return this.widget.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return this.widget.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return this.widget.charTyped(event);
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        return this.widget.nextFocusPath(navigationEvent);
    }

    @Override
    public boolean isFocused() {
        return this.widget.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);
        this.widget.setFocused(focused);
    }
}
