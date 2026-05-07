package com.nine.softimprints.client.ui.component;

import com.nine.softimprints.client.ui.context.EditorContext;
import com.nine.softimprints.client.ui.util.constant.SIColors;
import com.nine.softimprints.client.ui.util.constant.SITextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;

public class ProfileSwitchWidget extends AbstractWidget {

    private final EditorContext context;

    private final int labelPadding;
    private final int arrowWidth;
    private Component label = Component.empty();

    private boolean focusedLeft = false;
    private boolean focusedRight = false;

    private static final int ARROWS_GAP = 4;

    public ProfileSwitchWidget(
            int x,
            int y,
            int width,
            int height,
            EditorContext context
    ) {
        super(x, y, width, height, Component.empty());
        this.context = context;
        this.arrowWidth = 8;
        this.labelPadding = arrowWidth + 5 + ARROWS_GAP;
        context.addSelectedProfileListener(this::onSelectedProfileUpdate);

        onSelectedProfileUpdate();
    }

    private void onSelectedProfileUpdate(){
        this.label = Component.translatable(("profile.") + context.currentProfileId().toLanguageKey());
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float v
    ) {

        boolean left = onArrowLeft(mouseX, mouseY);
        boolean right = onArrowRight(mouseX, mouseY);
        boolean onText = onText(mouseX, mouseY);
        if (!left) focusedLeft = false;
        if (!right) focusedRight = false;

        int textColor = (onText ? SIColors.WHITE : SIColors.ALMOST_WHITE);

        graphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE)
                .acceptScrolling(
                        label.copy().withColor(textColor),
                        getX() + width / 2,
                        getX() + labelPadding,
                        getX() - labelPadding + width,
                        getY(), getY() + height
                );
        // arrows render
        // Left todo...

        int arrowY = getY() + 3;
        renderArrow(graphics, getX() + ARROWS_GAP, arrowY, 0, 0, left, focusedLeft);
        renderArrow(graphics, getX() + width - arrowWidth - ARROWS_GAP, arrowY, 16, 0, right, focusedRight);

    }

    private void renderArrow(GuiGraphicsExtractor graphics,
                             int x, int y, int u, int v, boolean hovered, boolean focused
    ){
        graphics.pose().pushMatrix();
        if (focused) {
            scaleAroundCenter(graphics, x, y, 8, 16, 0.93F);
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED,
                SITextures.ARROWS,
                x, y,
                u, hovered ? 8 + v : v,
                8, 16,
                4, 8,
                32, 32
        );
        graphics.pose().popMatrix();
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        boolean left = onArrowLeft(event.x(), event.y());
        boolean right = onArrowRight(event.x(), event.y());
        if (!this.isActive()) {
            return false;
        } else {
            if (this.isValidClickButton(event.buttonInfo())) {
                boolean isMouseOver = this.isMouseOver(event.x(), event.y());
                if (isMouseOver && (left || right)) {
                    this.playDownSound(Minecraft.getInstance().getSoundManager());
                    this.onClick(event, doubleClick);

                    if (left) focusedLeft = true;
                    if (right) focusedRight = true;

                    context.switchProfile(right);
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        focusedLeft = false;
        focusedRight = false;
        return super.mouseReleased(event);
    }


    private static void scaleAroundCenter(GuiGraphicsExtractor graphics, int x, int y, int w, int h, float scale) {
        float cx = x + w / 2.0F;
        float cy = y + h / 2.0F;

        graphics.pose().translate(cx, cy);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-cx, -cy);
    }


    private boolean onText(double mouseX, double mouseY){
        if (!isHovered()) return false;
        int actualTextW = Minecraft.getInstance().font.width(label);
        int labelFreeSpace = Math.max(0, (width - labelPadding * 2 - actualTextW));
        int lxStart = getX() + labelPadding + labelFreeSpace / 2;
        int lxEnd = lxStart + actualTextW;
        return (mouseX > lxStart) && (mouseX < lxEnd);
    }

    private boolean onArrowLeft(double mouseX, double mouseY){
        if (!isHovered()) return false;
        return (mouseX < getX() + arrowWidth + ARROWS_GAP * 2);
    }

    private boolean onArrowRight(double mouseX, double mouseY){
        if (!isHovered()) return false;
        return (mouseX > getX() + width - arrowWidth - ARROWS_GAP * 2);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
