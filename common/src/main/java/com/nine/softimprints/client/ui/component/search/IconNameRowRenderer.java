package com.nine.softimprints.client.ui.component.search;

import com.nine.softimprints.client.ui.util.constant.SIColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

public final class IconNameRowRenderer {

    public static final int ICON_SIZE = 16;
    public static final int ICON_TEXT_GAP = 3;

    private static final float SCROLL_SPEED = 12.0f;
    private static final long BOUNCE_PAUSE_MS = 1500L;

    private IconNameRowRenderer() {}

    public static void render(
            GuiGraphicsExtractor graphics,
            int x, int y, int width, int height,
            int mouseX, int mouseY,
            ItemStack iconStack, String name,
            boolean hovered, boolean selected
    ) {
        int iconY = y + (height - ICON_SIZE) / 2;

        int textX = x;
        int textWidth = width;

        if (renderIcon(graphics, iconStack, x, iconY)){
            textX += ICON_SIZE + ICON_TEXT_GAP;
            textWidth -= (ICON_SIZE + ICON_TEXT_GAP);
        }

        if (textWidth <= 0) return;

        Font font = Minecraft.getInstance().font;
        int textY = y + (height - font.lineHeight) / 2;
        int textColor = colorFor(hovered, selected);

        renderName(graphics, font, name, textX, textY, textWidth, y, height, textColor);
    }

    private static boolean renderIcon(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y) {
        if (!stack.isEmpty()) {
            try {
                graphics.item(stack, x, y);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static void renderName(
            GuiGraphicsExtractor graphics,
            Font font, String name,
            int textX, int textY, int textWidth,
            int rowY, int rowHeight,
            int color
    ) {
        int nameWidth = font.width(name);

        graphics.enableScissor(textX, rowY, textX + textWidth, rowY + rowHeight);
        try {
            if (nameWidth <= textWidth) {
                graphics.text(font, name, textX, textY, color, false);
            } else {
                int offset = bounceOffset(nameWidth - textWidth);
                graphics.text(font, name, textX - offset, textY, color, false);
            }
        } finally {
            graphics.disableScissor();
        }
    }

    private static int bounceOffset(int overflow) {
        long scrollMs = (long) (overflow / SCROLL_SPEED * 1000f);
        long cycleMs = BOUNCE_PAUSE_MS * 2 + scrollMs * 2;
        long t = System.currentTimeMillis() % cycleMs;

        if (t < BOUNCE_PAUSE_MS) {
            return 0;
        }
        if (t < BOUNCE_PAUSE_MS + scrollMs) {
            return (int) ((t - BOUNCE_PAUSE_MS) * (long) overflow / scrollMs);
        }
        if (t < BOUNCE_PAUSE_MS * 2L + scrollMs) {
            return overflow;
        }
        long elapsed = t - BOUNCE_PAUSE_MS * 2L - scrollMs;
        return overflow - (int) (elapsed * (long) overflow / scrollMs);
    }

    private static int colorFor(boolean hovered, boolean selected) {
        if (selected) return SIColors.WHITE;
        if (hovered) return SIColors.SOFT_SOFT_GRAY;
        return SIColors.GRAY;
    }
}
