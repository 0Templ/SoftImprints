package com.nine.softimprints.ui.component.preview.brush;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BrushHistory {

    private final List<BrushStroke> strokes = new ArrayList<>();

    private int generation = 0;

    public BrushHistory(){

    }

    public void add(BrushStroke s) {
        strokes.add(s); generation++;
    }

    public static List<BrushStroke> filterByDistance(List<BrushStroke> strokes, double distance) {
        var result = new ArrayList<BrushStroke>();
        boolean inDrag = false;
        BrushStroke prev = null;
        for (var s : strokes) {
            if (!s.drag()) {
                result.add(s);
                inDrag = false;
            } else {
                if (inDrag){
                    if (farEnough(prev, s, distance)){
                        result.add(s);
                        prev = s;
                    }
                }
                else {
                    inDrag = true;
                    prev = s;
                }
            }
        }
        return result;
    }

    private static boolean farEnough(BrushStroke a, BrushStroke b, double minDist) {
        double dx = a.x() - b.x();
        double dy = a.y() - b.y();
        return dx * dx + dy * dy >= minDist * minDist;
    }

    public static List<BrushStroke> deduplicateByCell(List<BrushStroke> strokes, double cellSize) {
        Map<Long, BrushStroke> dragByCell = new LinkedHashMap<>();
        var result = new ArrayList<BrushStroke>();
        for (var s : strokes) {
            if (!s.drag()) {
                result.add(s);
            } else {
                long cellX = Math.round(s.x() / cellSize);
                long cellY = Math.round(s.y() / cellSize);
                long key = (cellX << 32) | (cellY & 0xFFFFFFFFL);
                if (dragByCell.putIfAbsent(key, s) == null) {
                    result.add(s);
                }
            }
        }
        return result;
    }


    public void undo() {
        if (!strokes.isEmpty()) {
            strokes.removeLast(); generation++;
        }
    }

    public void clear() {
        strokes.clear();
        generation++;
    }

    public List<BrushStroke> snapshot() {
        return List.copyOf(strokes);
    }

    public int generation() {
        return generation;
    }

}
