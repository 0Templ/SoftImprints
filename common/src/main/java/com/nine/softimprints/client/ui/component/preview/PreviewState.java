package com.nine.softimprints.client.ui.component.preview;

import com.nine.softimprints.client.core.contact.ContactResult;
import com.nine.softimprints.client.core.stamp.*;
import com.nine.softimprints.client.profile.ImprintProfile;
import com.nine.softimprints.client.ui.component.preview.brush.BrushHistory;
import com.nine.softimprints.client.ui.component.preview.brush.BrushStroke;
import com.nine.softimprints.client.ui.draft.DraftHolder;

import java.util.Arrays;
import java.util.List;

public class PreviewState {

    private byte[] result;
    private int size;
    private int activeWidth;
    private int activeHeight;

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
        int size = Math.max(width, height);
        this.result = new byte[size*size];
        this.size = size;
        this.activeWidth = width;
        this.activeHeight = height;
        this.minAllowedDist = minAllowedDist;
        this.profileDraft = profileDraft;
        this.history = brushHistory;
    }

    public int mapSize(){
        return size;
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
        int nextSize = Math.max(width, height);
        boolean storageChanged = nextSize != this.size;
        this.size = nextSize;
        this.activeWidth = width;
        this.activeHeight = height;
        if (storageChanged) {
            this.result = new byte[nextSize * nextSize];
        }
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
        Arrays.fill(result, (byte) 0);
        if (profileDraft == null) return;
//        fillSimply(history.snapshot());

        var strokes = BrushHistory.deduplicateByCell(history.snapshot(), minAllowedDist * 0.5f);
        strokes = BrushHistory.filterByDistance(strokes, allowedDist);
        var profile = profileDraft.getDraft();
        strokes.forEach(s -> applyStroke(profile, s));

    }

    private void fillSimply(List<BrushStroke> strokes){
        for (var stroke : strokes){
            int cellX = (int) Math.floor(stroke.x());
            int cellY = (int) Math.floor(stroke.y());

            if (cellX < 0 || cellX >= size || cellY < 0 || cellY >= size) return;

            int index = cellY * size + cellX;
            this.result[index] = 1;
        }
    }

    private void applyStroke(ImprintProfile profile, BrushStroke stroke) {
        int contactSize = Math.max(1, (int) Math.ceil(stroke.size()));

        boolean[] contactMask = new boolean[contactSize * contactSize];
        Arrays.fill(contactMask, true);

        StampMask stamp = StampGenerator.generate(
                profile,
                contactMask,
                contactSize,
                stroke.seed(),
                ContactResult.StampStrategy.ELLIPSE,
                new StampProperties(0, 1, 1)
        );

        int originX = (int) Math.floor(stroke.x() - contactSize / 2.0) - stamp.padding();
        int originY = (int) Math.floor(stroke.y() - contactSize / 2.0) - stamp.padding();

        pasteStamp(stamp, originX, originY);
    }


    private void pasteStamp(StampMask stampMask, int originX, int originY){
        var src = stampMask.mask();
        int stampSize = stampMask.size();
        for (int sy = 0; sy < stampSize; sy++){
            int dy = sy + originY;
            if (dy < 0) continue;
            if (dy >= activeHeight) break;
            for (int sx = 0; sx < stampSize; sx++) {
                int dx = sx + originX;
                if (dx < 0) continue;
                if (dx >= activeWidth) break;

                int srcIndex = sy * stampSize + sx;
                int dstIndex = dy * size + dx;

                var value = src[srcIndex];


                if (value == 0) continue;
                var cur = result[dstIndex];
                if (cur == 0 || (value & 0xFF) < (cur & 0xFF)){
                    result[dstIndex] = value;
                }
            }
        }
    }

    public byte[] getResult(){
        return this.result;
    }


}
