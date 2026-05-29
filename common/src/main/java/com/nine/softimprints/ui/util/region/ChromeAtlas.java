package com.nine.softimprints.ui.util.region;

import com.nine.softimprints.ui.util.constant.SITextures;

public final class ChromeAtlas {

    private static final int TEXTURE_SIZE = 128;
    private static final int FRAME_WIDTH = 64;
    private static final int FRAME_HEIGHT = 16;
    private static final int BORDER = 2;

    private ChromeAtlas() {
    }

    public static final Frame TAB = frame(0);
    public static final Frame SECTION = frame(16);
    public static final Frame TAB_HOVERED = frame(32);

    public static final UIRegion BORDER_LIGHT_FILL = region(1, 1, 1, 1);
    public static final UIRegion BORDER_DARK_FILL = region(0, 0, 1, 1);

    public static final UIRegion BLACK_FILL = region(112, 0, 16, 16);
    public static final UIRegion TAB_HOVER_ACCENT = region(1, 35, 1, 1);

    public static final UIRegion WHITE_FILL = region(1, 33, 1, 1);

    public static UIRegion region(int u, int v, int w, int h) {
        return UIRegion.of(SITextures.UI_CHROME, u, v, w, h, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static Frame frame(int y) {
        return new Frame(
                region(0, y, BORDER, BORDER),
                region(FRAME_WIDTH - BORDER, y, BORDER, BORDER),
                region(0, y + FRAME_HEIGHT - BORDER, BORDER, BORDER),
                region(FRAME_WIDTH - BORDER, y + FRAME_HEIGHT - BORDER, BORDER, BORDER),
                region(BORDER, y, FRAME_WIDTH - BORDER * 2 - 1, BORDER),
                region(BORDER, y + FRAME_HEIGHT - BORDER, FRAME_WIDTH - BORDER * 2 - 1, BORDER),
                region(0, y + BORDER, BORDER, FRAME_HEIGHT - BORDER * 2),
                region(FRAME_WIDTH - BORDER, y + BORDER, BORDER, FRAME_HEIGHT - BORDER * 2)
        );
    }

    public record Frame(
            UIRegion topLeft,
            UIRegion topRight,
            UIRegion bottomLeft,
            UIRegion bottomRight,
            UIRegion top,
            UIRegion bottom,
            UIRegion left,
            UIRegion right
    ) {
        public BoxSkin skin() {
            return new BoxSkin(topLeft, topRight, bottomLeft, bottomRight, top, bottom, left, right, BORDER);
        }
    }
}
