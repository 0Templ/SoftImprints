package com.nine.softimprints.client.ui.component.list.element;

import com.nine.softimprints.client.ui.util.constant.SIColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BooleanSupplier;

public class LabelEntry extends AbstractConfigListEntry {

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
    private final Font font;
    private final int linesGap;

    private List<FormattedCharSequence> wrappedLines = List.of();

    public LabelEntry(int gap, Component text) {
        this(gap, text, null);
    }

    public LabelEntry(int gap, Component text, @Nullable Component tooltip) {
        this(gap, text, tooltip, null, null, () -> false);
    }

    public LabelEntry(
            int gap,
            Component text,
            @Nullable Component tooltip,
            @Nullable Component marker,
            @Nullable Component markerTooltip,
            BooleanSupplier markerVisible
    ) {
        super(0);
        this.linesGap = gap;
        this.text = text;
        this.tooltip = tooltip;
        this.marker = marker;
        this.markerTooltip = markerTooltip;
        this.markerVisible = markerVisible;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void updateEntryLayout(int x, int y, int width) {
        super.updateEntryLayout(x, y, width);

        this.wrappedLines = font.split(text, availableTextWidth());
        int totalHeight = wrappedLines.isEmpty()
                ? 0
                : wrappedLines.size() * font.lineHeight + (wrappedLines.size() - 1) * linesGap;

        updateHeight(totalHeight);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int y = getY();
        boolean textHovered = isMouseOverText(mouseX, mouseY);
        for (FormattedCharSequence line : wrappedLines) {
            graphics.centeredText(
                    font,
                    line,
                    getX() + getWidth() / 2,
                    y,
                    textHovered ? SIColors.WHITE : SIColors.ALMOST_WHITE
            );
            y += font.lineHeight + linesGap;
        }

        boolean markerHovered = renderMarker(graphics, mouseX, mouseY);

        if (tooltip != null && !markerHovered && textHovered) {
            graphics.setTooltipForNextFrame(font, font.split(tooltip, TOOLTIP_MAX_WIDTH), mouseX, mouseY);
        }
    }

    private boolean renderMarker(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
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
        int markerReserve = marker == null ? 0 : MARKER_SIZE + MARKER_GAP;
        return Math.max(1, getWidth() - markerReserve);
    }

    private boolean isMouseOverText(int mouseX, int mouseY) {
        if (!isMouseOver(mouseX, mouseY) || wrappedLines.isEmpty()) {
            return false;
        }

        int y = getY();
        for (FormattedCharSequence line : wrappedLines) {
            int lineWidth = font.width(line);
            int x0 = getX() + (getWidth() - lineWidth) / 2;
            int x1 = x0 + lineWidth;
            int y1 = y + font.lineHeight;
            if (mouseX >= x0 && mouseX < x1 && mouseY >= y && mouseY < y1) {
                return true;
            }
            y += font.lineHeight + linesGap;
        }
        return false;
    }
}
