package com.nine.softimprints.client.ui.component.preview;

import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.core.contact.raster.ContactRaster;
import com.nine.softimprints.client.core.contact.raster.StampRaster;
import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.stamp.StampGenerator;
import com.nine.softimprints.client.core.stamp.StampProperties;
import com.nine.softimprints.client.core.stamp.StampSeedHelper;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.ui.component.preview.brush.BrushHistory;
import com.nine.softimprints.client.ui.component.preview.brush.BrushStroke;
import com.nine.softimprints.client.ui.draft.DraftHolder;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class PreviewState {

    private int activeWidth;
    private int activeHeight;
    private final Map<Long, byte[]> columns = new LinkedHashMap<>();

    public final BrushHistory history;
    private boolean dragging = false;
    private double lastX, lastY;
    private final double minAllowedDist;
    private double allowedDist;

    private DraftHolder<ImprintProfile> profileDraft;

    public PreviewState(int size, double minAllowedDist, DraftHolder<ImprintProfile> profileDraft, BrushHistory brushHistory) {
        this(size, size, minAllowedDist, profileDraft, brushHistory);
    }

    public PreviewState(
            int width, int height,
            double minAllowedDist,
            DraftHolder<ImprintProfile> profileDraft,
            BrushHistory brushHistory
    ){
        this.activeWidth = width;
        this.activeHeight = height;
        this.minAllowedDist = minAllowedDist;
        this.profileDraft = profileDraft;
        this.history = brushHistory;
    }

    public int mapSize(){
        return currentMapSize();
    }

    public int activeWidth() {
        return activeWidth;
    }

    public int activeHeight() {
        return activeHeight;
    }

    public void setProfileDraft(DraftHolder<ImprintProfile> profileDraft) {
        this.profileDraft = profileDraft;
    }

    public void setMaskSize(int width, int height) {
        this.activeWidth = width;
        this.activeHeight = height;
        rebuild();
    }

    public void setAllowedDist(double dist){
        this.allowedDist = Math.max(minAllowedDist, dist);
        rebuild();
    }

    public void setDragging(boolean value){
        if (dragging != value){
            this.lastX = -1;
            this.lastY = -1;
        }
        this.dragging = value;
    }

    public void addStroke(double normX, double normY, double size) {
        int seed = StampSeedHelper.mixSeed(Double.hashCode(normX), Double.hashCode(normY));

        if (dragging) {
            if (lastX != -1 && lastY != -1){
                var dx = normX - lastX;
                var dy = normY - lastY;
                var dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < minAllowedDist){
                    return;
                }
            }
            this.lastX = normX;
            this.lastY = normY;
        }
        history.add(new BrushStroke(normX, normY, size, seed, dragging));

        // add checks
        rebuild();
    }

    public void clear() {
        history.clear();
        rebuild();
    }

    public void rebuild() {
        columns.clear();
        if (profileDraft == null) return;

        var strokes = BrushHistory.deduplicateByCell(history.snapshot(), minAllowedDist * 0.5f);
        strokes = BrushHistory.filterByDistance(strokes, allowedDist);
        var profile = profileDraft.getDraft();
        strokes.forEach(s -> applyStroke(profile, s));

    }

    private void applyStroke(ImprintProfile profile, BrushStroke stroke) {
        int mapSize = Math.max(1, profile.resolution().mapSize());
        double profileScale = mapSize
                / (double) Constants.PREVIEW_BLOCK_RESOLUTION;
        int contactSize = Math.max(1, (int) Math.ceil(stroke.size() * profileScale));

        boolean[] contactMask = new boolean[contactSize * contactSize];
        Arrays.fill(contactMask, true);

        double contactOriginX = Math.floor(stroke.x() * profileScale - contactSize / 2.0D);
        double contactOriginY = Math.floor(stroke.y() * profileScale - contactSize / 2.0D);

        ContactRaster contact = new ContactRaster(
                contactOriginX, contactOriginY, 1.0D,
                contactSize, contactSize,
                contactMask
        );

        StampRaster stamp = StampGenerator.generate(
                profile,
                contact,
                stroke.seed(),
                ContactResult.StampStrategy.ELLIPSE,
                new StampProperties(0, 1, 1)
        );

        pasteStamp(stamp, mapSize);
    }


    private void pasteStamp(StampRaster stampRaster, int mapSize){
        var src = stampRaster.mask();
        int w = stampRaster.width();
        int h = stampRaster.height();
        int originX = (int) Math.floor(stampRaster.originX());
        int originY = (int) Math.floor(stampRaster.originZ());

        int blockWidth = Math.ceilDiv(activeWidth, Constants.PREVIEW_BLOCK_RESOLUTION);
        int blockHeight = Math.ceilDiv(activeHeight, Constants.PREVIEW_BLOCK_RESOLUTION);

        for (int sy = 0; sy < h; sy++){
            int globalY = originY + sy;
            int blockY = Math.floorDiv(globalY, mapSize);
            if (blockY < 0) continue;
            if (blockY >= blockHeight) break;
            int localY = Math.floorMod(globalY, mapSize);

            for (int sx = 0; sx < w; sx++) {
                byte value = src[sy * w + sx];
                if (value == 0) continue;

                int globalX = originX + sx;
                int blockX = Math.floorDiv(globalX, mapSize);
                if (blockX < 0) continue;
                if (blockX >= blockWidth) break;
                int localX = Math.floorMod(globalX, mapSize);

                byte[] column = columns.computeIfAbsent(columnKey(blockX, blockY), key -> new byte[mapSize * mapSize]);
                int dstIndex = localY * mapSize + localX;
                byte cur = column[dstIndex];
                if (cur == 0 || (value & 0xFF) < (cur & 0xFF)){
                    column[dstIndex] = value;
                }
            }
        }
    }

    public Map<Long, byte[]> columns() {
        return Collections.unmodifiableMap(columns);
    }

    public int currentMapSize() {
        if (profileDraft == null) return Constants.PREVIEW_BLOCK_RESOLUTION;
        return Math.max(1, profileDraft.getDraft().resolution().mapSize());
    }

    public static long columnKey(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }

    public static int columnX(long key) {
        return (int) (key >> 32);
    }

    public static int columnY(long key) {
        return (int) key;
    }


}
