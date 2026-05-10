package com.nine.softimprints.client.ui.component.preview.widget;

import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.profile.options.texture.ImprintTextureSet;
import com.nine.softimprints.client.ui.cache.UICache;
import com.nine.softimprints.client.ui.component.preview.PreviewState;
import com.nine.softimprints.client.ui.context.EditorContext;
import com.nine.softimprints.client.ui.context.PreviewSettings;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprintPreviewWidget extends AbstractWidget {

    private final Map<Byte, Identifier> guiTexturesByValue = new HashMap<>();
    private final List<PreviewRect> previewCache = new ArrayList<>();

    private final EditorContext editorContext;
    private final PreviewSettings previewSettings;
    private final PreviewState previewState;

    private final int maxResolution = 128;
    private int viewX;
    private int viewY;
    private int viewResolution;
    private int viewWidth;
    private int viewHeight;

    public ImprintPreviewWidget(
            int x,
            int y,
            int width,
            int height,
            PreviewSettings previewSettings,
            EditorContext editorContext
    ) {
        super(x, y, width, height, Component.empty());
        this.editorContext = editorContext;
        this.previewSettings = previewSettings;

        this.previewState = new PreviewState(
                maxResolution,
                1D, editorContext.currentProfile(),
                UICache.brushHistory()
        );

        this.viewResolution = Math.clamp(previewSettings.resolution(), 1, maxResolution);

        syncAllowedDistanceFromConfig();
        applyViewResolution();

        this.rebuildRenderCache();

        editorContext.config().draft(SIConfig.General.IMPRINT_STEP_DISTANCE).addListener(this::onStepDistanceChanged);
        editorContext.addSelectedProfileListener(() ->
                this.previewState.setProfileDraft(editorContext.currentProfile())
        );
        editorContext.addSelectedProfileDraftListener(() -> {
            this.previewState.rebuild();
            this.rebuildRenderCache();
        });
        editorContext.addOnCloseListeners(() -> UICache.setBrushHistory(previewState.history));
    }

    private void onStepDistanceChanged() {
        syncAllowedDistanceFromConfig();
        rebuildRenderCache();
    }

    private void syncAllowedDistanceFromConfig() {
        double stepDistanceBlocks = editorContext.config().draftValue(SIConfig.General.IMPRINT_STEP_DISTANCE);
        previewState.setAllowedDist(stepDistanceBlocks * Constants.BASIC_RESOLUTION);
    }


    public int maxResolution() {
        int activeWidth = previewState.activeWidth();
        int activeHeight = previewState.activeHeight();
        double widgetAspect = width / (double) height;

        if (widgetAspect >= 1.0D) {
            return Math.max(1, Math.min(
                    activeHeight,
                    (int) Math.floor(activeWidth / widgetAspect)
            ));
        } else {
            return Math.max(1, Math.min(
                    activeWidth,
                    (int) Math.floor(activeHeight * widgetAspect)
            ));
        }
    }

    private record PreviewRect(byte value, int x0, int y0, int x1, int y1) {}

    public void refreshRenderCache() {
        rebuildRenderCache();
    }

    private void rebuildRenderCache(){
        var holder = editorContext.currentProfile();

        guiTexturesByValue.clear();
        previewCache.clear();
        var profile = holder.getDraft();
        var textureSet = previewSettings.debugMode()
                ? ImprintTextureSet.DEBUG_SET
                : profile.textureSets.getCurrent();
        if (textureSet == null) {
            return;
        }
        textureSet.texturesByValue().forEach((key, identifier) -> {
            guiTexturesByValue.put(key, toGuiTexture(identifier));
        });
        guiTexturesByValue.put((byte) 0, toGuiTexture(profile.textureSets.zeroLayer()));

        byte[] map = previewState.getResult();
        int stride = previewState.mapSize();

        int xEnd = viewX + visibleWidth();
        int yEnd = viewY + visibleHeight();

        for (int y = viewY; y < yEnd; y++) {
            int x = viewX;

            while (x < xEnd) {
                byte value = map[y * stride + x];
                int x0 = x++;
                while (x < xEnd && map[y * stride + x] == value) {
                    x++;
                }
                if (value != 0) {
                    previewCache.add(new PreviewRect(value, x0, y, x, y + 1));
                }
            }
        }
    }

    private Identifier toGuiTexture(Identifier identifier){
        return Identifier.parse(identifier.getNamespace() + ":textures/" + identifier.getPath() + ".png");
    }

    public void clearPreview(){
        this.previewState.clear();
        this.rebuildRenderCache();
    }

    @Override
    protected void extractWidgetRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a
    ) {
        renderPreviewMap(graphics);
    }

    private void renderPreviewMap(GuiGraphicsExtractor graphics) {
        int visibleWidth = visibleWidth();
        int visibleHeight = visibleHeight();
        if (visibleWidth <= 0 || visibleHeight <= 0) return;

        graphics.pose().pushMatrix();
        try {
            graphics.pose().translate(getX(), getY());
            graphics.pose().scale(
                    width / (float) visibleWidth,
                    height / (float) visibleHeight
            );

            renderBaseLayerLogical(graphics, guiTexturesByValue.get((byte) 0));
            renderPreviewLogical(graphics);
        } finally {
            graphics.pose().popMatrix();
        }
    }

    private void renderBaseLayerLogical(GuiGraphicsExtractor graphics, Identifier texture) {
        renderTiledRect(
                graphics,
                texture,
                0,
                0,
                visibleWidth(),
                visibleHeight(),
                viewX,
                viewY
        );
    }

    private void renderPreviewLogical(GuiGraphicsExtractor graphics) {
        for (PreviewRect rect : previewCache) {
            if (rect.value() == 0) continue;

            Identifier texture = guiTexturesByValue.get(rect.value());
            if (texture == null) continue;

            int x = rect.x0() - viewX;
            int y = rect.y0() - viewY;
            int w = rect.x1() - rect.x0();
            int h = rect.y1() - rect.y0();
            renderTiledRect(graphics, texture, x, y, x + w, y + h, rect.x0(), rect.y0());
        }
    }

    private void renderTiledRect(
            GuiGraphicsExtractor graphics,
            Identifier texture,
            int localX0,
            int localY0,
            int localX1,
            int localY1,
            int mapX0,
            int mapY0
    ) {
        for (int y = localY0; y < localY1; ) {
            int mapY = mapY0 + (y - localY0);
            int v = Math.floorMod(mapY, Constants.BASIC_RESOLUTION);
            int h = Math.min(localY1 - y, Constants.BASIC_RESOLUTION - v);

            for (int x = localX0; x < localX1; ) {
                int mapX = mapX0 + (x - localX0);
                int u = Math.floorMod(mapX, Constants.BASIC_RESOLUTION);
                int w = Math.min(localX1 - x, Constants.BASIC_RESOLUTION - u);

            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    texture,
                    x, y,
                    u, v,
                    w, h,
                    w, h,
                    16, 16
            );

                x += w;
            }

            y += h;
        }
    }


    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (!isMouseOver(event.x(), event.y())){
            previewState.setDragging(false);
            return super.mouseDragged(event, dx, dy);
        }

        previewState.setDragging(true);

        addStrokeAt(event.x(), event.y());

        return true;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        return addStrokeAt(event.x(), event.y());
    }

    public void setViewResolution(int resolution) {
        this.viewResolution = Math.clamp(resolution, 1, maxResolution);
        applyViewResolution();
        clampViewOrigin();
        refreshRenderCache();
    }

    public void setViewSize(int width, int height) {
        this.viewWidth = Math.clamp(width, 1, previewState.activeWidth());
        this.viewHeight = Math.clamp(height, 1, previewState.activeHeight());
        clampViewOrigin();
        refreshRenderCache();
    }

    public void setViewOrigin(int x, int y) {
        this.viewX = x;
        this.viewY = y;
        clampViewOrigin();
        refreshRenderCache();
    }

    public void setMaskSize(int width, int height) {
        int maskWidth = Math.clamp(width, 1, maxResolution);
        int maskHeight = Math.clamp(height, 1, maxResolution);
        this.previewState.setMaskSize(maskWidth, maskHeight);
        syncAllowedDistanceFromConfig();
        applyViewResolution();
        clampViewOrigin();
        refreshRenderCache();
    }

    private boolean addStrokeAt(double mouseX, double mouseY) {
        MapPoint point = toMapPoint(mouseX, mouseY);
        if (point == null) return false;
        previewState.addStroke(point.x(), point.y(), previewSettings.brushSize());

        refreshRenderCache();

        return true;
    }

    private MapPoint toMapPoint(double mouseX, double mouseY) {
        int visibleWidth = visibleWidth();
        int visibleHeight = visibleHeight();
        if (visibleWidth <= 0 || visibleHeight <= 0) return null;

        double localX = mouseX - getX();
        double localY = mouseY - getY();
        if (localX < 0 || localY < 0 || localX >= width || localY >= height) return null;

        double x = viewX + (localX / width) * visibleWidth;
        double y = viewY + (localY / height) * visibleHeight;
        if (x < 0 || y < 0 || x >= previewState.activeWidth() || y >= previewState.activeHeight()) return null;

        return new MapPoint(x, y);
    }

    private int visibleWidth() {
        return Math.max(0, Math.min(viewWidth, previewState.activeWidth() - viewX));
    }

    private int visibleHeight() {
        return Math.max(0, Math.min(viewHeight, previewState.activeHeight() - viewY));
    }

    private void clampViewOrigin() {
        int maxX = Math.max(0, previewState.activeWidth() - viewWidth);
        int maxY = Math.max(0, previewState.activeHeight() - viewHeight);
        this.viewX = Math.clamp(viewX, 0, maxX);
        this.viewY = Math.clamp(viewY, 0, maxY);
    }

    private void applyViewResolution() {
        int activeWidth = previewState.activeWidth();
        int activeHeight = previewState.activeHeight();
        double widgetAspect = width / (double) height;

        if (widgetAspect >= 1.0D) {
            this.viewHeight = Math.clamp(viewResolution, 1, activeHeight);
            this.viewWidth = (int) Math.ceil(viewHeight * widgetAspect);
            if (viewWidth > activeWidth) {
                this.viewWidth = activeWidth;
                this.viewHeight = Math.clamp((int) Math.floor(viewWidth / widgetAspect), 1, activeHeight);
            }
        } else {
            this.viewWidth = Math.clamp(viewResolution, 1, activeWidth);
            this.viewHeight = (int) Math.ceil(viewWidth / widgetAspect);
            if (viewHeight > activeHeight) {
                this.viewHeight = activeHeight;
                this.viewWidth = Math.clamp((int) Math.floor(viewHeight * widgetAspect), 1, activeWidth);
            }
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        previewState.setDragging(false);
        return false;
    }


    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    private record MapPoint(double x, double y) {
    }
}
