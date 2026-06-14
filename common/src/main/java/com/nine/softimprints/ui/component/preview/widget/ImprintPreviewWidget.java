package com.nine.softimprints.ui.component.preview.widget;

import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.Constants;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.catalog.entry.InvalidProfileEntry;
import com.nine.softimprints.profile.options.texture.ImprintTextureSet;
import com.nine.softimprints.ui.cache.UICache;
import com.nine.softimprints.ui.component.preview.PreviewState;
import com.nine.softimprints.ui.context.EditorContext;
import com.nine.softimprints.ui.context.PreviewSettings;
import com.nine.softimprints.ui.draft.DraftHolder;
import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImprintPreviewWidget extends AbstractWidget {

    private static final int MAX_PREVIEW_MAP_SIZE = 128;

    private final EditorContext editorContext;
    private final PreviewSettings previewSettings;
    private final PreviewState previewState;

    private final Map<Byte, TextureAtlasSprite> spritesByValue = new HashMap<>();
    private final List<RenderColumn> renderColumns = new ArrayList<>();

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

        DraftHolder<ImprintProfile> draft = editorContext.currentDraft();

        this.previewState = new PreviewState(
                MAX_PREVIEW_MAP_SIZE,
                1D, draft,
                UICache.brushHistory()
        );

        this.viewResolution = Math.clamp(previewSettings.resolution(), 1, maxResolution());

        syncAllowedDistanceFromConfig();
        applyViewResolution();

        this.rebuildRenderCache();

        editorContext.config().draft(SIConfig.General.IMPRINT_STEP_DISTANCE).addListener(this::onStepDistanceChanged);
        editorContext.addSelectedProfileListener(this::syncProfileState);
        editorContext.addSelectedProfileDraftListener(this::syncProfileState);
        editorContext.addOnCloseListeners(() -> UICache.setBrushHistory(previewState.history));
    }

    private void onStepDistanceChanged() {
        syncAllowedDistanceFromConfig();
        rebuildRenderCache();
    }

    private void syncAllowedDistanceFromConfig() {
        double stepDistanceBlocks = editorContext.config().draftValue(SIConfig.General.IMPRINT_STEP_DISTANCE);
        previewState.setAllowedDist(stepDistanceBlocks * Constants.PREVIEW_BLOCK_RESOLUTION);
    }

    private void syncProfileState() {
        previewState.setProfileDraft(editorContext.currentDraft());

        syncAllowedDistanceFromConfig();
        applyViewResolution();
        clampViewOrigin();
        rebuildRenderCache();
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

    public void refreshRenderCache() {
        rebuildRenderCache();
    }

    private void rebuildRenderCache() {
        spritesByValue.clear();
        renderColumns.clear();
        var profile = editorContext.currentProfile();
        if (profile != null) {
            updateSpriteCache(profile);
            updateRenderCache();
        }
    }

    private void updateRenderCache() {
        int mapSize = previewState.mapSize();
        var columns = previewState.columns();

        for (var column : columns.entrySet()) {
            int blockX = PreviewState.columnX(column.getKey());
            int blockY = PreviewState.columnY(column.getKey());

            var map = column.getValue();

            List<MaskRect> rects = new ArrayList<>();

            for (int y = 0; y < mapSize; y++) {
                int row = y * mapSize;
                int x = 0;

                while (x < mapSize) {
                    byte value = map[row + x];
                    int x0 = x++;

                    while (x < mapSize && map[row + x] == value) {
                        x++;
                    }
                    if (value != 0) {
                        rects.add(new MaskRect(value, x0, y, x, y + 1));
                    }
                }
            }
            if (!rects.isEmpty()) {
                RenderColumn columnMask = new RenderColumn(blockX, blockY, mapSize, rects);
                renderColumns.add(columnMask);
            }
        }
    }

    private void updateSpriteCache(@Nonnull ImprintProfile profile) {
        var textureSet = previewSettings.debugMode()
                ? ImprintTextureSet.DEBUG_SET
                : profile.textureSets.getCurrent();
        if (textureSet == null) {
            return;
        }
        textureSet.texturesByValue().keySet().forEach(key -> spritesByValue.put(key, textureSet.spriteFor(key)));
        var atlas = (TextureAtlas) Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS);
        spritesByValue.put((byte) 0, atlas.getSprite(profile.preview.base()));
    }

    public void clearPreview() {
        this.previewState.clear();
        this.rebuildRenderCache();
    }

    @Override
    protected void extractWidgetRenderState(
            @NonNull GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float a
    ) {


        if (editorContext.currentEntry() instanceof InvalidProfileEntry entry) {
            int x = getX() + 2;
            int w = getWidth() - 4;
            int y = getY() + 2;
            int h = getHeight() - 4;

            int centreX = x + w / 2;
            int centreY = y + h / 2;

            var font = Minecraft.getInstance().font;
            graphics.centeredText(
                    font,
                    Component.translatable("imprint_profile.issue.unsupported_profile"),
                    centreX, centreY + font.lineHeight / 2, SIColors.SOFT_SOFT_GRAY
            );
            return;
        }
        renderPreviewMap(graphics);
    }

    private void renderPreviewMap(GuiGraphicsExtractor graphics) {
        int visibleWidth = visibleWidth();
        int visibleHeight = visibleHeight();
        int mapSize = previewState.mapSize();
        if (visibleWidth <= 0 || visibleHeight <= 0 || mapSize <= 0) return;

        float visibleMapWidth = visibleWidth * mapSize / (float) Constants.PREVIEW_BLOCK_RESOLUTION;
        float visibleMapHeight = visibleHeight * mapSize / (float) Constants.PREVIEW_BLOCK_RESOLUTION;
        float viewMapX = viewX * mapSize / (float) Constants.PREVIEW_BLOCK_RESOLUTION;
        float viewMapY = viewY * mapSize / (float) Constants.PREVIEW_BLOCK_RESOLUTION;
        var pose = graphics.pose();

        pose.pushMatrix();
        graphics.enableScissor(getX(), getY(), getX() + width, getY() + height);

        try {
            pose.translate(getX(), getY());
            pose.scale(width / visibleMapWidth, height / visibleMapHeight);
            pose.translate(-viewMapX, -viewMapY);

            renderBaseLayer(graphics, mapSize);
            renderPreviewLayers(graphics);
        } finally {
            graphics.disableScissor();
            pose.popMatrix();
        }

    }

    private void renderBaseLayer(
            GuiGraphicsExtractor graphics,
            int mapSize
    ) {
        TextureAtlasSprite sprite = spritesByValue.get((byte) 0);
        if (sprite == null) return;

        int blockRes = Constants.PREVIEW_BLOCK_RESOLUTION;
        int viewX1 = viewX + visibleWidth();
        int viewY1 = viewY + visibleHeight();
        int firstBlockX = Math.floorDiv(viewX, blockRes);
        int firstBlockY = Math.floorDiv(viewY, blockRes);
        int lastBlockX = Math.floorDiv(Math.max(viewX, viewX1 - 1), blockRes);
        int lastBlockY = Math.floorDiv(Math.max(viewY, viewY1 - 1), blockRes);

        for (int blockY = firstBlockY; blockY <= lastBlockY; blockY++) {
            for (int blockX = firstBlockX; blockX <= lastBlockX; blockX++) {
                int x0 = blockX * mapSize;
                int y0 = blockY * mapSize;
                graphics.blit(
                        sprite.atlasLocation(),
                        x0, y0,
                        x0 + mapSize, y0 + mapSize,
                        sprite.getU0(), sprite.getU1(),
                        sprite.getV0(), sprite.getV1()
                );
            }
        }

    }

    private void renderPreviewLayers(GuiGraphicsExtractor graphics) {
        int blockRes = Constants.PREVIEW_BLOCK_RESOLUTION;
        int viewX1 = viewX + visibleWidth();
        int viewY1 = viewY + visibleHeight();

        for (var column : renderColumns) {
            int blockPreviewX = column.blockX() * blockRes;
            int blockPreviewY = column.blockY() * blockRes;
            if (blockPreviewX + blockRes <= viewX
                    || blockPreviewY + blockRes <= viewY
                    || blockPreviewX >= viewX1
                    || blockPreviewY >= viewY1) {
                continue;
            }

            for (var rect : column.rectList) {
                int blockMapX = column.blockX() * column.mapSize();
                int blockMapY = column.blockY() * column.mapSize();
                renderRect(graphics, rect, blockMapX, blockMapY, column.mapSize());
            }
        }
    }

    private void renderRect(
            GuiGraphicsExtractor graphics,
            MaskRect rect,
            int x,
            int y,
            int mapSize
    ) {
        var sprite = spritesByValue.get(rect.value());
        if (sprite == null) return;

        float u0 = sprite.getU(rect.x0() / (float) mapSize);
        float u1 = sprite.getU(rect.x1() / (float) mapSize);
        float v0 = sprite.getV(rect.y0() / (float) mapSize);
        float v1 = sprite.getV(rect.y1() / (float) mapSize);

        int x0 = x + rect.x0();
        int y0 = y + rect.y0();
        int x1 = x + rect.x1();
        int y1 = y + rect.y1();

        graphics.blit(sprite.atlasLocation(), x0, y0, x1, y1, u0, u1, v0, v1);
    }

    @Override
    public boolean mouseDragged(
            MouseButtonEvent event,
            double dx,
            double dy
    ) {
        if (!isMouseOver(event.x(), event.y())) {
            previewState.setDragging(false);
            return super.mouseDragged(event, dx, dy);
        }

        previewState.setDragging(true);

        addStrokeAt(event.x(), event.y());

        return true;
    }

    @Override
    public boolean mouseScrolled(
            double x,
            double y,
            double scrollX,
            double scrollY
    ) {
        return false;
    }

    @Override
    public boolean mouseClicked(
            MouseButtonEvent event,
            boolean doubleClick
    ) {
        return addStrokeAt(event.x(), event.y());
    }

    public void setViewResolution(int resolution) {
        this.viewResolution = Math.clamp(resolution, 1, maxResolution());
        applyViewResolution();
        clampViewOrigin();
        refreshRenderCache();
    }

    private boolean addStrokeAt(
            double mouseX,
            double mouseY
    ) {
        MapPoint point = toMapPoint(mouseX, mouseY);
        if (point == null) return false;
        previewState.addStroke(point.x(), point.y(), previewSettings.brushSize());

        refreshRenderCache();

        return true;
    }

    private MapPoint toMapPoint(
            double mouseX,
            double mouseY
    ) {
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
    public boolean mouseReleased(@NonNull MouseButtonEvent event) {
        previewState.setDragging(false);
        return false;
    }

    @Override
    protected void updateWidgetNarration(@NonNull NarrationElementOutput narrationElementOutput) {

    }

    private record RenderColumn(int blockX, int blockY, int mapSize, List<MaskRect> rectList) {
    }

    private record MaskRect(
            byte value,
            int x0, int y0,
            int x1, int y1
    ) {
    }

    private record MapPoint(double x, double y) {
    }
}
