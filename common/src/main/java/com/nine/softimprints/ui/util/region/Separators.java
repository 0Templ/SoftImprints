package com.nine.softimprints.ui.util.region;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class Separators {

	private Separators() {}

	public static void renderHorizontal(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, int width) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, 0.0F, 0.0F, width, 2, 32, 2);
	}
}
