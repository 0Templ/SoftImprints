package com.nine.softimprints.ui.component.preview.widget;

import com.mojang.blaze3d.platform.NativeImage;
import com.nine.softimprints.SICommon;
import com.nine.softimprints.mixin.accessor.client.SpriteContentsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;
import java.util.concurrent.atomic.AtomicInteger;

final class PreviewCanvas implements AutoCloseable {

    private static final int MAX_PIXELS_PER_CELL = 4;

    private static final AtomicInteger INSTANCE_IDS = new AtomicInteger();

    private final Identifier location;

    @Nullable
    private DynamicTexture texture;
    private int blocksX;
    private int blocksY;
    private int mapSize;
    private int pixelsPerCell = 1;
    private boolean dirty;

    PreviewCanvas(String name) {
        this.location = Identifier.fromNamespaceAndPath(SICommon.MODID,
                "preview_canvas/" + name + "_" + INSTANCE_IDS.incrementAndGet());
    }

    Identifier location() {
        return location;
    }

    boolean isReady() {
        return texture != null;
    }

    void reset(
            int blocksX,
            int blocksY,
            int mapSize,
            int maxSpriteSize
    ) {
        int perCell = Math.clamp(Math.ceilDiv(Math.max(1, maxSpriteSize), Math.max(1, mapSize)), 1, MAX_PIXELS_PER_CELL);
        int width = blocksX * mapSize * perCell;
        int height = blocksY * mapSize * perCell;

        if (texture == null
                || this.blocksX != blocksX || this.blocksY != blocksY
                || this.mapSize != mapSize || this.pixelsPerCell != perCell) {
            close();
            this.blocksX = blocksX;
            this.blocksY = blocksY;
            this.mapSize = mapSize;
            this.pixelsPerCell = perCell;
            this.texture = new DynamicTexture(location::toString, width, height, true);
            Minecraft.getInstance().getTextureManager().register(location, texture);
        } else {
            NativeImage pixels = texture.getPixels();
            if (pixels != null) {
                pixels.fillRect(0, 0, width, height, 0);
            }
        }
        this.dirty = true;
    }

    void composeColumn(
            int blockX,
            int blockY,
            @Nullable byte[] map,
            TextureAtlasSprite[] spritesByValue
    ) {
        if (texture == null) return;
        if (blockX < 0 || blockX >= blocksX || blockY < 0 || blockY >= blocksY) return;
        NativeImage pixels = texture.getPixels();
        if (pixels == null) return;

        int blockPixels = mapSize * pixelsPerCell;
        int originX = blockX * blockPixels;
        int originY = blockY * blockPixels;
        if (map == null) {
            pixels.fillRect(originX, originY, blockPixels, blockPixels, 0);
            this.dirty = true;
            return;
        }

        for (int py = 0; py < blockPixels; py++) {
            int cellRow = (py / pixelsPerCell) * mapSize;
            int canvasY = originY + py;

            for (int px = 0; px < blockPixels; px++) {
                byte value = map[cellRow + px / pixelsPerCell];
                int color = 0;
                if (value != 0) {
                    TextureAtlasSprite sprite = spritesByValue[value & 0x7F];
                    if (sprite != null) {
                        var contents = sprite.contents();
                        NativeImage source = ((SpriteContentsAccessor) contents).si$originalImage();
                        if (source.format() == NativeImage.Format.RGBA) {
                            int tx = px * contents.width() / blockPixels;
                            int ty = py * contents.height() / blockPixels;
                            color = source.getPixel(tx, ty);
                        }
                    }
                }
                pixels.setPixel(originX + px, canvasY, color);
            }
        }
        this.dirty = true;
    }

    void uploadIfDirty() {
        if (!dirty || texture == null) return;
        texture.upload();
        dirty = false;
    }

    @Override
    public void close() {
        if (texture != null) {
            Minecraft.getInstance().getTextureManager().release(location);
            texture = null;
        }
    }

}
