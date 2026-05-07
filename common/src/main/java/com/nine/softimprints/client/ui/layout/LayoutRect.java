package com.nine.softimprints.client.ui.layout;

public record LayoutRect(int x, int y, int width, int height) {

    public int right() {
        return x + width;
    }

    public int bottom() {
        return y + height;
    }

    public LayoutRect inset(int value) {
        return inset(value, value, value, value);
    }

    public LayoutRect inset(int left, int top, int right, int bottom) {
        int nextX = x + left;
        int nextY = y + top;
        int nextW = Math.max(0, width - left - right);
        int nextH = Math.max(0, height - top - bottom);
        return new LayoutRect(nextX, nextY, nextW, nextH);
    }

    public LayoutRect centeredSquare() {
        int side = Math.max(0, Math.min(width, height));
        return new LayoutRect(
                x + (width - side) / 2,
                y + (height - side) / 2,
                side,
                side
        );
    }
}
