package com.nine.softimprints.client.ui.util.region;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public final class ChromeRenderer {

    private ChromeRenderer() {
    }

    public static void blackFill(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        ChromeAtlas.BLACK_FILL.render(graphics, x, y, width, height);
    }

    public static void headerLine(GuiGraphicsExtractor graphics, int x, int y, int width) {
        ChromeAtlas.SECTION.top().render(graphics, x, y, width, 2);
    }

    public static void footerLine(GuiGraphicsExtractor graphics, int x, int y, int width) {
        ChromeAtlas.SECTION.bottom().render(graphics, x, y, width, 2);
    }

    public static void leftDivider(GuiGraphicsExtractor graphics, int x, int y, int height) {
        ChromeAtlas.SECTION.left().render(graphics, x, y, 2, height);
    }

    public static void rightDivider(GuiGraphicsExtractor graphics, int x, int y, int height) {
        ChromeAtlas.SECTION.right().render(graphics, x, y, 2, height);
    }

    public static void sectionBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        BoxRenderer.render(graphics, BoxSkins.SECTION, x, y, width, height);
    }

    public static void tabBorder(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            int width,
            int height,
            boolean active,
            boolean hovered
    ) {
        BoxSkin skin = hovered ? BoxSkins.TAB_HOVERED : BoxSkins.TAB;
        BoxRenderer.render(graphics, skin, x, y, width, height - 2, BorderSides.NO_BOTTOM);

        if (active) {
            UIRegion side = hovered ? ChromeAtlas.TAB_HOVERED.bottom() : ChromeAtlas.TAB.bottom();
            side.render(graphics, x + 1, y + height - 2, 1, 2);
            side.render(graphics, x + width - 2, y + height - 2, 1, 2);
            ChromeAtlas.TAB.bottom().render(graphics, x, y + height - 2, 1, 2);
            ChromeAtlas.TAB.bottom().render(graphics, x + width - 1, y + height - 2, 1, 2);
        } else {
            ChromeAtlas.TAB.bottom().render(graphics, x, y + height - 2, width, 2);
            if (hovered) {
                ChromeAtlas.TAB_HOVER_ACCENT.render(graphics, x + 2, y + height - 3, width - 4, 1);
            }
        }
    }
}
