package com.nine.softimprints.ui.component.group;

import com.nine.softimprints.ui.util.constant.SIColors;
import com.nine.softimprints.ui.util.region.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

public record OptionEntry<T>(T value, int width, Component label, TabEdge tabEdge, boolean fixedWidth) implements GroupEntry<T> {

    private static final int TAB_INACTIVE_INSET = 4;

    public OptionEntry(T value, Component label, TabEdge tabEdge) {
        this(value, Minecraft.getInstance().font.width(label), label, tabEdge, false);
    }

    public OptionEntry(T value, Component label, TabEdge tabEdge, int fixedWidth) {
        this(value, fixedWidth, label, tabEdge, true);
    }

    public OptionEntry(T value, int width, Component label, TabEdge tabEdge) {
        this(value, width, label, tabEdge, false);
    }

    @Override
    public boolean interactive() {
        return true;
    }

    @Override
    public GroupEntry<T> withWidth(int width) {
        return new OptionEntry<>(value, width, label, tabEdge, fixedWidth);
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
        if (!active) {
            switch (tabEdge){
                case BOTTOM -> {
                    // return;
                }
                case TOP -> {
                    y += TAB_INACTIVE_INSET;
                }
            }
            height -= TAB_INACTIVE_INSET;
        }
		
		final int border = 2;

		int bgX = x + border;
		int bgY = y + border;
		int bgW = this.width - border * 2;
		int bgH = height - border * 2;

        if (active) {
            switch (tabEdge) {
                case TOP -> {
                    bgH += 2;
                }
                case BOTTOM -> {
                    bgY -= 2;
                    bgH += 2;
                }
            };
            Screen.extractMenuBackgroundTexture(
                    graphics, Screen.MENU_BACKGROUND,
					bgX, bgY,
                    0,
                    0,
					bgW, bgH
            );
        } else {
			ChromeRenderer.blackFill(graphics, bgX, bgY, bgW, bgH);
        }
        tabBorder(graphics, x, y, width, height, active, hovered, tabEdge);
        renderMarker(graphics, marker, x, y, height, active);

        int textColor = active ? SIColors.WHITE : (hovered ? SIColors.WHITE : 0xEEEEEE);
        graphics.textRendererForWidget(owner, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrollingWithDefaultCenter(label.copy().withColor(textColor),
                        x + 2, x + this.width - 3,
                        y + 2, y + height - 2);

        if (active) {
            int lineWidth = Math.min(width - 10, Minecraft.getInstance().font.width(label) - 8);
            lineWidth = Math.max(6, lineWidth);
            int lineX = x + (this.width - lineWidth) / 2;

            int underlineY = 0;
            switch (tabEdge){
                case BOTTOM -> {
                    underlineY = y + 1;
                }
                case TOP -> {
                    underlineY = y + height - 2;
                }
            }
            ChromeAtlas.WHITE_FILL.render(graphics, lineX, underlineY, lineWidth, 1);
        }
    }

    private void renderMarker(
            GuiGraphicsExtractor graphics,
            GroupMarker marker,
            int x,
            int y,
            int height,
            boolean active
    ) {
        if (marker == null) return;
        switch (marker) {
            case UPDATE -> renderUpdateMarker(graphics, x, y, height, active);
        }
    }

    private void renderUpdateMarker(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int height,
            boolean active
    ) {
        if (active) return;
        int pixelSize = 3;
        int shineWidth = 6;
        long sweepDurationMs = 680;
        long cooldownMs = sweepDurationMs * 5;
        long cycleMs = sweepDurationMs + cooldownMs;

        long phaseMs = Util.getMillis() % cycleMs;
        if (phaseMs > sweepDurationMs) {
            return;
        }

        float t = phaseMs / (float) sweepDurationMs;

        int shineSpan = shineWidth * pixelSize;
        int travel = width + height + shineSpan * 2;
        int offset = Math.round(t * travel) - height - shineSpan;

        graphics.enableScissor(x + 2, y + 2, x + width - 2, y + height - 2);

        for (int row = 0; row < height; row += pixelSize) {
            for (int i = 0; i < shineWidth; i++) {
                float center = (shineWidth - 1) / 2.0f;
                float dist = Math.abs(i - center);

                float normalized = dist / (center + 1.0f);
                int alpha = (int) (45 * (1.0f - normalized));

                if (alpha <= 0) continue;

                int color = (alpha << 24) | 0xFFFFFF;
                int px = x + offset + row + i * pixelSize;

                graphics.fill(px, y + row, px + pixelSize, y + row + pixelSize, color);
            }
        }
        graphics.disableScissor();
    }

    public static void tabBorder(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            boolean active,
            boolean hovered,
            TabEdge edge
    ) {
        BoxSkin skin = hovered ? BoxSkins.TAB_HOVERED : BoxSkins.TAB;
        switch (edge) {
            case TOP -> {
                BoxRenderer.render(graphics, skin, x, y, width, height - 2, BorderSides.NO_BOTTOM);
            }
            case BOTTOM -> {
                BoxRenderer.render(graphics, skin, x, y + 2, width, height - 2, BorderSides.NO_TOP);
                //return;
                // return;
            }
        }
        if (active) {
            UIRegion side;
            switch (edge) {
                case TOP -> {
                    side = hovered ? ChromeAtlas.TAB_HOVERED.bottom() : ChromeAtlas.TAB.bottom();
                    side.render(graphics, x + 1, y + height - 2, 1, 2);
                    side.render(graphics, x + width - 2, y + height - 2, 1, 2);
                    ChromeAtlas.TAB.bottom().render(graphics, x, y + height - 2, 1, 2);
                    ChromeAtlas.TAB.bottom().render(graphics, x + width - 1, y + height - 2, 1, 2);
                }
                case BOTTOM -> {
                    side = hovered ? ChromeAtlas.TAB_HOVERED.top() : ChromeAtlas.TAB.top();
                    side.render(graphics, x + 1, y, 1, 2);
                    side.render(graphics, x + width - 2, y, 1, 2);
                    ChromeAtlas.TAB.top().render(graphics, x, y, 1, 2);
                    ChromeAtlas.TAB.top().render(graphics, x + width - 1, y, 1, 2);
                }
            }
        } else {
            switch (edge) {
                case TOP -> {
                    ChromeAtlas.TAB.bottom().render(graphics, x, y + height - 2, width, 2);
                    if (hovered) {
                        ChromeAtlas.TAB_HOVER_ACCENT.render(graphics, x + 2, y + height - 3, width - 4, 1);
                    }
                }
                case BOTTOM -> {
                    ChromeAtlas.TAB.top().render(graphics, x, y, width, 2);

                }
            }

        }
    }

}
