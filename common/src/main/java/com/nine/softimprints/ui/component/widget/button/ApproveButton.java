package com.nine.softimprints.ui.component.widget.button;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ApproveButton extends Button {

    private final Component baseText;
    private final Component approveText;
    private boolean ready = false;

    public ApproveButton(
            int x,
            int y,
            int width,
            int height,
            Component baseText,
            Component approveText,
            Component tooltip,
            OnPress onPress
    ) {
        super(x, y, width, height, Component.empty(), onPress, Button.DEFAULT_NARRATION);
        this.baseText = baseText;
        this.approveText = approveText;
        this.setTooltip(Tooltip.create(tooltip));
    }

    public static ApproveButton base(
            int x,
            int y,
            int width,
            int height,
            Component tooltip,
            OnPress onPress
    ) {
        return new ApproveButton(
                x, y, width, height,
                Component.translatable("config.softimprints.reset"),
                Component.translatable("config.softimprints.reset.approve"),
                tooltip, onPress
        );
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        var ret = super.mouseClicked(event, doubleClick);
        return ret;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        if (!ready) {
            ready = true;
            setFocused(true);
            return;
        }
        ready = false;
        setFocused(false);
        this.onPress.onPress(this);
    }

    @Override
    public void setFocused(boolean focused) {
        if (!focused) {
            ready = false;
        }
        super.setFocused(focused);
    }

    @Override
    protected void extractContents(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a
    ) {
        this.extractDefaultSprite(graphics);
        this.extractScrollingStringOverContents(graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE),
                ready ? approveText : baseText, 2);
    }

}
