package com.nine.softimprints.client.ui.component.group;

import com.nine.softimprints.client.ui.util.constant.SIColors;
import com.nine.softimprints.client.ui.util.region.ChromeAtlas;
import com.nine.softimprints.client.ui.util.region.ChromeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public record OptionEntry<T>(T value, int width, Component label) implements GroupEntry<T> {

    private static final int TAB_INACTIVE_INSET = 4;

    public OptionEntry(T value, Component label) {
        this(value, Minecraft.getInstance().font.width(label), label);
    }

    @Override
    public boolean interactive() {
        return true;
    }

    @Override
    public GroupEntry<T> withWidth(int width) {
        return new OptionEntry<>(value, width, label);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, AbstractWidget owner, int x, int y, int height, boolean active, boolean hovered) {
        if (!active) {
            y += TAB_INACTIVE_INSET;
            height -= TAB_INACTIVE_INSET;
        }
		
		final int border = 2;

		int bgX = x + border;
		int bgY = y + border;
		int bgW = this.width - border * 2;
		int bgH = height - border * 2;

        if (active) {
            Screen.extractMenuBackgroundTexture(
                    graphics, Screen.MENU_BACKGROUND,
					bgX, bgY,
                    0,
                    0,
					bgW, bgH + 2
            );
        } else {
			ChromeRenderer.blackFill(graphics, bgX, bgY, bgW, bgH);
        }
		ChromeRenderer.tabBorder(graphics, x, y, width, height, active, hovered);
		
        int textColor = active ? SIColors.WHITE : (hovered ? SIColors.WHITE : 0xEEEEEE);
        graphics.textRendererForWidget(owner, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrollingWithDefaultCenter(label.copy().withColor(textColor),
                        x + 2, x + this.width - 3,
                        y + 2, y + height - 2);

        if (active) {
            int lineWidth = Math.min(width - 10, Minecraft.getInstance().font.width(label) - 8);
            int lineX = x + (this.width - lineWidth) / 2;
            ChromeAtlas.WHITE_FILL.render(graphics, lineX, y + height - 2, lineWidth, 1);
//            blitTab(graphics, lineX, y + height - 2, lineWidth, 1,
//                    1, 17, lineWidth, 1);
        }
    }


}
