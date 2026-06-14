package com.nine.softimprints.ui.component.list.element;

import com.nine.softimprints.ui.component.widget.LabelWidget;
import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.client.gui.ComponentPath;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.function.BooleanSupplier;

public class LabelEntry extends AbstractConfigListEntry {

    private final LabelWidget widget;

    public LabelEntry(
            int gap,
            Component text
    ) {
        this(gap, text, (Component) null);
    }

    public LabelEntry(
            int gap,
            Component text,
            int labelColor,
            int labelColorHovered
    ) {
        this(gap, text, labelColor, labelColorHovered, null, null, null, () -> false, null, LabelWidget.Mode.WRAPPED);
    }

    public LabelEntry(
            int gap,
            Component text,
            @Nullable Component tooltip
    ) {
        this(gap, text, tooltip, null, null, () -> false);
    }

    public LabelEntry(
            int gap,
            Component text,
            @Nullable Runnable onClickAction
    ) {
        this(gap, text, null, onClickAction);
    }

    public LabelEntry(
            int gap,
            Component text,
            @Nullable Component tooltip,
            @Nullable Runnable onClickAction
    ) {
        this(gap, text, SIColors.GRAY, SIColors.ALMOST_WHITE, tooltip, null, null, () -> false,
                onClickAction, LabelWidget.Mode.WRAPPED);
    }

    public LabelEntry(
            int gap,
            Component text,
            @Nullable Component tooltip,
            @Nullable Component marker,
            @Nullable Component markerTooltip,
            BooleanSupplier markerVisible
    ) {
        this(gap, text, SIColors.GRAY, SIColors.ALMOST_WHITE, tooltip, marker, markerTooltip, markerVisible,
                null, LabelWidget.Mode.WRAPPED);
    }

    public LabelEntry(
            int gap,
            Component text,
            int labelColor,
            int labelColorHovered,
            @Nullable Component tooltip,
            @Nullable Component marker,
            @Nullable Component markerTooltip,
            BooleanSupplier markerVisible,
            @Nullable Runnable onClickAction,
            LabelWidget.Mode mode
    ) {
        super(0);
        this.widget = new LabelWidget(
                text,
                gap,
                labelColor,
                labelColorHovered,
                tooltip,
                marker,
                markerTooltip,
                markerVisible,
                onClickAction,
                mode
        );
    }

    public static LabelEntry singleLine(
            int gap,
            Component text
    ) {
        return singleLine(gap, text, null, null);
    }

    public static LabelEntry singleLine(
            int gap,
            Component text,
            @Nullable Component tooltip
    ) {
        return singleLine(gap, text, tooltip, null);
    }

    public static LabelEntry singleLine(
            int gap,
            Component text,
            @Nullable Component tooltip,
            @Nullable Runnable onClickAction
    ) {
        return new LabelEntry(gap, text, SIColors.GRAY, SIColors.ALMOST_WHITE, tooltip, null, null,
                () -> false, onClickAction, LabelWidget.Mode.SINGLE_LINE);
    }

    public static Runnable openLinkAction(URI uri) {
        return LabelWidget.openLinkAction(uri);
    }

    @Override
    public void updateEntryLayout(
            int x,
            int y,
            int width
    ) {
        super.updateEntryLayout(x, y, width);
        this.widget.updateLayout(x, y, width);
        this.updateHeight(widget.getHeight());
    }

    @Override
    public boolean isFocusable() {
        return widget.isFocusable();
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
