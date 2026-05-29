package com.nine.softimprints.ui.component.list.element;

import com.nine.softimprints.ui.util.constant.SIColors;
import com.nine.softimprints.ui.util.region.ChromeAtlas;
import com.nine.softimprints.ui.util.region.ChromeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

public class SeparatorListEntry extends AbstractConfigListEntry {

    @Nullable
    private final Component label;

    private final Font font;

    public SeparatorListEntry(@Nullable Component label) {
        super(2);
        this.font = Minecraft.getInstance().font;
        this.label = label;
        updateHeight(font.lineHeight);
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float partialTick
    ) {
        if (label != null){
            // Todo: tests
            if (true) return;
            int labelW = font.width(label);
            int centerX = getX() + getWidth() / 2;

            int labelX = centerX - labelW / 2;
            int labelRight = labelX + labelW;

            int gap = 3;
            int lineY = getY() + getHeight() / 2 - 1;

            int leftX = getX();
            int leftW = Math.max(0, labelX - gap - leftX);

            int rightX = labelRight + gap;
            int rightW = Math.max(0, getX() + getWidth() - rightX);

            ChromeRenderer.footerLine(graphics, leftX, lineY, leftW);
            ChromeRenderer.footerLine(graphics, rightX, lineY, rightW);

            ChromeAtlas.BORDER_DARK_FILL.render(graphics, rightX - 2, lineY - 1, 3, 1);
            ChromeAtlas.BORDER_LIGHT_FILL.render(graphics, rightX - 2, lineY, 1, 1);
            ChromeAtlas.BORDER_LIGHT_FILL.render(graphics, rightX - 1, lineY, 1, 2);
            graphics.centeredText(font, label, centerX, getY(), SIColors.GRAY);

        }
        else {
            ChromeRenderer.footerLine(graphics, getX(), getY(), getWidth());
        }
    }

    @Override
    public boolean fullWidth() {
        return true;
    }
}
