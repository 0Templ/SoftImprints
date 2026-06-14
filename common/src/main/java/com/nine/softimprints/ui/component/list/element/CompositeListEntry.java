package com.nine.softimprints.ui.component.list.element;

import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositeListEntry extends AbstractConfigListEntry {

    protected final List<AbstractWidget> widgets = new ArrayList<>();

    private AbstractWidget focusedWidget;
    private AbstractWidget activeMouseWidget;

    protected CompositeListEntry(int height) {
        super(height);
    }

    public void addWidget(AbstractWidget widget) {
        widgets.add(widget);
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        for (AbstractWidget widget : widgets) {
            widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void mouseMoved(
            double x,
            double y
    ) {
        for (AbstractWidget widget : widgets) {
            widget.mouseMoved(x, y);
        }
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseClicked(event, doubleClick)) {
                setFocusedWidget(widget);
                activeMouseWidget = widget;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (activeMouseWidget == null) {
            return false;
        }

        AbstractWidget widget = activeMouseWidget;
        activeMouseWidget = null;
        return widget.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dragX,
            double dragY
    ) {
        return activeMouseWidget != null && activeMouseWidget.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(
            double x,
            double y,
            double scrollX,
            double scrollY
    ) {
        for (AbstractWidget widget : widgets) {
            if (widget.mouseScrolled(x, y, scrollX, scrollY)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return focusedWidget != null && focusedWidget.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return focusedWidget != null && focusedWidget.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return focusedWidget != null && focusedWidget.charTyped(event);
    }

    @Override
    public ComponentPath nextFocusPath(FocusNavigationEvent navigationEvent) {
        if (focusedWidget != null) {
            ComponentPath path = focusedWidget.nextFocusPath(navigationEvent);
            if (path != null) {
                return path;
            }
        }

        for (var widget : widgets) {
            ComponentPath path = widget.nextFocusPath(navigationEvent);
            if (path != null) {
                return path;
            }
        }

        return null;
    }

    @Override
    public boolean isFocused() {
        return focusedWidget != null && focusedWidget.isFocused();
    }

    @Override
    public void setFocused(boolean focused) {
        super.setFocused(focused);

        if (!focused && focusedWidget != null) {
            focusedWidget.setFocused(false);
            focusedWidget = null;
        }
        if (focused && focusedWidget == null && !widgets.isEmpty()) {
            setFocusedWidget(widgets.getFirst());
        }
    }

    @Override
    public boolean isFocusable() {
        return !widgets.isEmpty();
    }

    private void setFocusedWidget(AbstractWidget widget) {
        if (focusedWidget == widget) {
            return;
        }

        if (focusedWidget != null) {
            focusedWidget.setFocused(false);
        }

        focusedWidget = widget;

        if (focusedWidget != null) {
            focusedWidget.setFocused(true);
        }
    }
}
