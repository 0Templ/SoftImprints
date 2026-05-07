package com.nine.softimprints.client.ui.util.region;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public record UIRegion (
		Identifier texture,
		int u0, int v0, int u1, int v1,
		int textureW, int textureH) {

	public static UIRegion of(Identifier texture, int u, int v, int w, int h, int texW, int texH) {
		return new UIRegion(texture, u, v, u + w, v + h, texW, texH);
	}

	public void render(GuiGraphicsExtractor graphics, int x, int y, int w, int h) {
		graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
				x, y, u0, v0,
				w, h,
				u1 - u0, v1 - v0, textureW, textureH);
	}
}
