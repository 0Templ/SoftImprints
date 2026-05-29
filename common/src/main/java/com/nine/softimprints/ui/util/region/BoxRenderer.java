package com.nine.softimprints.ui.util.region;

import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.EnumSet;

public final class BoxRenderer {

	public static void render(GuiGraphicsExtractor g, BoxSkin skin,
			int x, int y, int w, int h, EnumSet<BorderSides> sides) {
		int b = skin.border();
		if (w < 2 * b || h < 2 * b) return;

		boolean top = sides.contains(BorderSides.TOP);
		boolean bottom = sides.contains(BorderSides.BOTTOM);
		boolean left = sides.contains(BorderSides.LEFT);
		boolean right = sides.contains(BorderSides.RIGHT);

		int innerX = left ? x + b : x;
		int innerY = top ? y + b : y;
		int innerW = w - (left ? b : 0) - (right ? b : 0);
		int innerH = h - (top ? b : 0) - (bottom ? b : 0);

		if (top) skin.top().render(g, innerX, y, innerW, b);
		if (bottom) skin.bottom().render(g, innerX, y + h - b, innerW, b);
		if (left) skin.left().render(g, x, innerY, b, innerH);
		if (right) skin.right().render(g, x + w - b, innerY, b, innerH);

		if (top && left) skin.topLeft().render(g, x, y, b, b);
		if (top && right) skin.topRight().render(g, x + w - b, y, b, b);
		if (bottom && left) skin.botLeft().render(g, x, y + h - b, b, b);
		if (bottom && right) skin.botRight().render(g, x + w - b, y + h - b, b, b);
	}

	public static void render(GuiGraphicsExtractor g, BoxSkin skin, int x, int y, int w, int h) {
		render(g, skin, x, y, w, h, BorderSides.FULL);
	}
}
