package com.nine.softimprints.ui.component.widget;

import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Util;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.List;
import java.util.function.BooleanSupplier;

public class LabelWidget extends AbstractWidget {

    private static final int MARKER_SIZE = 11;
    private static final int MARKER_GAP = 4;
    private static final int TOOLTIP_MAX_WIDTH = 160;
    private static final int MARKER_COLOR = 0xFFFF8800;
    private static final int MARKER_COLOR_HOVERED = 0xFFFFAA00;
    private final Component text;
    @Nullable
    private final Component tooltip;
    @Nullable
    private final Component marker;
    @Nullable
    private final Component markerTooltip;
    private final BooleanSupplier markerVisible;
    @Nullable
    private final Runnable onClickAction;
    private final Font font;
    private final int linesGap;
    private final int labelColor;
    private final int labelColorHovered;
    private final Mode mode;
    private List<FormattedCharSequence> wrappedLines = List.of();

    public LabelWidget(
            Component text,
            int linesGap,
            int labelColor,
            int labelColorHovered,
            @Nullable Component tooltip,
            @Nullable Component marker,
            @Nullable Component markerTooltip,
            BooleanSupplier markerVisible,
            @Nullable Runnable onClickAction,
            Mode mode
    ) {
        super(0, 0, 0, 0, text);
        this.text = text;
        this.linesGap = linesGap;
        this.labelColor = labelColor;
        this.labelColorHovered = labelColorHovered;
        this.tooltip = tooltip;
        this.marker = marker;
        this.markerTooltip = markerTooltip;
        this.markerVisible = markerVisible;
        this.onClickAction = onClickAction;
        this.mode = mode;
        this.font = Minecraft.getInstance().font;
        if (tooltip != null) {
            this.setTooltip(Tooltip.create(tooltip));
        }
    }

    public static LabelWidget wrapped(
            Component text,
            int linesGap
    ) {
        return wrapped(text, linesGap, null, null);
    }

    public static LabelWidget wrapped(
            Component text,
            int linesGap,
            @Nullable Component tooltip
    ) {
        return wrapped(text, linesGap, tooltip, null);
    }

    public static LabelWidget wrapped(
            Component text,
            int linesGap,
            @Nullable Runnable onClickAction
    ) {
        return wrapped(text, linesGap, null, onClickAction);
    }

    public static LabelWidget wrapped(
            Component text,
            int linesGap,
            @Nullable Component tooltip,
            @Nullable Runnable onClickAction
    ) {
        return new LabelWidget(text, linesGap, SIColors.GRAY, SIColors.ALMOST_WHITE, tooltip,
                null, null, () -> false, onClickAction, Mode.WRAPPED);
    }

    public static LabelWidget singleLine(Component text) {
        return singleLine(text, null, null);
    }

    public static LabelWidget singleLine(
            Component text,
            @Nullable Component tooltip
    ) {
        return singleLine(text, tooltip, null);
    }

    public static LabelWidget singleLine(
            Component text,
            @Nullable Runnable onClickAction
    ) {
        return singleLine(text, null, onClickAction);
    }

    public static LabelWidget singleLine(
            Component text,
            @Nullable Component tooltip,
            @Nullable Runnable onClickAction
    ) {
        return new LabelWidget(text, 0, SIColors.GRAY, SIColors.ALMOST_WHITE, tooltip,
                null, null, () -> false, onClickAction, Mode.SINGLE_LINE);
    }


    public static LabelWidget singleLine(
            Component text,
            int labelColor,
            int labelColorHovered
    ) {
        return new LabelWidget(text, 0, labelColor, labelColorHovered, null,
                null, null, () -> false, null, Mode.SINGLE_LINE);
    }

    public static LabelWidget singleLine(
            Component text,
            int labelColor,
            int labelColorHovered,
            @Nullable Runnable onClickAction
    ) {
        return new LabelWidget(text, 0, labelColor, labelColorHovered, null,
                null, null, () -> false, onClickAction, Mode.SINGLE_LINE);
    }

    public static LabelWidget singleLine(
            Component text,
            int labelColor,
            int labelColorHovered,
            @Nullable Component tooltip,
            @Nullable Runnable onClickAction
    ) {
        return new LabelWidget(text, 0, labelColor, labelColorHovered, tooltip,
                null, null, () -> false, onClickAction, Mode.SINGLE_LINE);
    }

    public static LabelWidget link(
            Component text,
            URI uri
    ) {
        return singleLine(text, openLinkAction(uri));
    }

    public static LabelWidget link(
            Component text,
            Component tooltip,
            URI uri
    ) {
        return singleLine(text, tooltip, openLinkAction(uri));
    }

    /// /
    public static Runnable openLinkAction(URI uri) {
        return () -> openLink(uri);
    }

    private static void openLink(URI uri) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen previous = minecraft.screen;
        minecraft.setScreen(new ConfirmLinkScreen(
                confirmed -> {
                    if (confirmed) {
                        Util.getPlatform().openUri(uri);
                    }
                    minecraft.setScreen(previous);
                },
                uri.toString(),
                true
        ));
    }

    public void updateLayout(
            int x,
            int y,
            int width
    ) {
        this.setX(x);
        this.setY(y);
        this.setWidth(width);
        this.refreshHeight();
    }

    public int preferredHeight(int width) {
        if (mode == Mode.SINGLE_LINE) {
            return font.lineHeight;
        }

        List<FormattedCharSequence> lines = font.split(text, Math.max(1, availableTextWidth(width)));
        return lines.isEmpty()
                ? 0
                : lines.size() * font.lineHeight + (lines.size() - 1) * linesGap;
    }

    private void refreshHeight() {
        if (mode == Mode.SINGLE_LINE) {
            this.wrappedLines = List.of();
        } else {
            this.wrappedLines = font.split(text, availableTextWidth());
        }
        this.setHeight(preferredHeight(getWidth()));
    }

    public boolean isFocusable() {
        return false;
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        boolean textHovered = isMouseOverText(mouseX, mouseY);
        if (mode == Mode.SINGLE_LINE) {
            renderSingleLineText(graphics, textHovered);
        } else {
            renderWrappedText(graphics, textHovered);
        }

        boolean markerHovered = renderMarker(graphics, mouseX, mouseY);
        if (tooltip != null && !markerHovered && textHovered) {
            graphics.setTooltipForNextFrame(font, font.split(tooltip, TOOLTIP_MAX_WIDTH), mouseX, mouseY);
        }
    }

    private void renderSingleLineText(
            GuiGraphicsExtractor graphics,
            boolean hovered
    ) {
        int color = hovered ? labelColorHovered : labelColor;
        int textY = getY() + Math.max(0, (getHeight() - font.lineHeight) / 2);
        graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrollingWithDefaultCenter(
                        text.copy().withColor(color),
                        getX(),
                        getX() + availableTextWidth(),
                        textY,
                        textY + font.lineHeight - 1
                );
    }

    private void renderWrappedText(
            GuiGraphicsExtractor graphics,
            boolean hovered
    ) {
        int y = getY();
        int color = hovered ? labelColorHovered : labelColor;
        for (FormattedCharSequence line : wrappedLines) {
            graphics.centeredText(
                    font,
                    line,
                    getX() + availableTextWidth() / 2,
                    y,
                    color
            );
            y += font.lineHeight + linesGap;
        }
    }

    private boolean renderMarker(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY
    ) {
        if (marker == null || !markerVisible.getAsBoolean()) {
            return false;
        }

        int markerX = getX() + getWidth() - MARKER_SIZE;
        int markerY = getY() + Math.max(0, (getHeight() - font.lineHeight) / 2) + 1;
        boolean hovered = mouseX >= markerX
                && mouseX < markerX + MARKER_SIZE
                && mouseY >= getY()
                && mouseY < getY() + getHeight();

        graphics.text(
                font,
                marker.getString(),
                markerX + 2,
                markerY,
                hovered ? MARKER_COLOR_HOVERED : MARKER_COLOR,
                false
        );

        if (hovered && markerTooltip != null) {
            graphics.setTooltipForNextFrame(font, font.split(markerTooltip, TOOLTIP_MAX_WIDTH), mouseX, mouseY);
        }
        return hovered;
    }

    private int availableTextWidth() {
        return availableTextWidth(getWidth());
    }

    private int availableTextWidth(int width) {
        int markerReserve = marker == null ? 0 : MARKER_SIZE + MARKER_GAP;
        return Math.max(1, width - markerReserve);
    }

    private boolean isMouseOverText(
            int mouseX,
            int mouseY
    ) {
        if (!isMouseOver(mouseX, mouseY)) {
            return false;
        }

        if (mode == Mode.SINGLE_LINE) {
            int lineWidth = Math.min(font.width(text), availableTextWidth());
            int x0 = getX() + (availableTextWidth() - lineWidth) / 2;
            int x1 = x0 + lineWidth;
            int y0 = getY() + Math.max(0, (getHeight() - font.lineHeight) / 2);
            return mouseX >= x0
                    && mouseX < x1
                    && mouseY >= y0
                    && mouseY < y0 + font.lineHeight;
        }

        if (wrappedLines.isEmpty()) {
            return false;
        }

        int y = getY();
        for (FormattedCharSequence line : wrappedLines) {
            int lineWidth = font.width(line);
            int x0 = getX() + (availableTextWidth() - lineWidth) / 2;
            int x1 = x0 + lineWidth;
            int y1 = y + font.lineHeight;
            if (mouseX >= x0 && mouseX < x1 && mouseY >= y && mouseY < y1) {
                return true;
            }
            y += font.lineHeight + linesGap;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        if (event.button() == 0 && onClickAction != null && isMouseOverText((int) event.x(), (int) event.y())) {
            onClickAction.run();
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        this.defaultButtonNarrationText(output);
    }

    public enum Mode {
        WRAPPED,
        SINGLE_LINE
    }
}
