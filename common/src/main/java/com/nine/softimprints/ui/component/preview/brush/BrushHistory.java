package com.nine.softimprints.ui.component.preview.brush;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BrushHistory {

    private final List<BrushStroke> strokes = new ArrayList<>();

    private int generation = 0;

    public BrushHistory() {

    }

    public static final class Acceptor {

        private final double cellSize;
        private final double distance;
        private final Set<Long> seenDragCells = new HashSet<>();
        private boolean inDrag;
        private BrushStroke prev;

        public Acceptor(
                double cellSize,
                double distance
        ) {
            this.cellSize = cellSize;
            this.distance = distance;
        }

        public boolean accept(BrushStroke s) {
            if (!s.drag()) {
                inDrag = false;
                return true;
            }
            long cellX = Math.round(s.x() / cellSize);
            long cellY = Math.round(s.y() / cellSize);
            long key = (cellX << 32) | (cellY & 0xFFFFFFFFL);
            if (!seenDragCells.add(key)) {
                return false;
            }
            if (!inDrag) {
                inDrag = true;
                prev = s;
                return false;
            }
            if (!farEnough(prev, s, distance)) {
                return false;
            }
            prev = s;
            return true;
        }

        private static boolean farEnough(
                BrushStroke a,
                BrushStroke b,
                double minDist
        ) {
            double dx = a.x() - b.x();
            double dy = a.y() - b.y();
            return dx * dx + dy * dy >= minDist * minDist;
        }
    }

    public void add(BrushStroke s) {
        strokes.add(s);
        generation++;
    }

    public void undo() {
        if (!strokes.isEmpty()) {
            strokes.removeLast();
            generation++;
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
