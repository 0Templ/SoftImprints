package com.nine.softimprints.ui.util.region;

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


}
