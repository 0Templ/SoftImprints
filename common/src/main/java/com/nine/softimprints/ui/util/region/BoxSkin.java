package com.nine.softimprints.ui.util.region;

public record BoxSkin(
        UIRegion topLeft, UIRegion topRight, UIRegion botLeft, UIRegion botRight,
        UIRegion top, UIRegion bottom, UIRegion left, UIRegion right,
        int border
) {
    public static Builder builder(int border) {
        return new Builder(border);
    }

    public static final class Builder {
        private final int border;
        private UIRegion topLeft, topRight, botLeft, botRight;
        private UIRegion top, bottom, left, right;

        private Builder(int border) {
            this.border = border;
        }

        public Builder corners(
                UIRegion topLeft,
                UIRegion topRight,
                UIRegion botLeft,
                UIRegion botRight
        ) {
            this.topLeft = topLeft;
            this.topRight = topRight;
            this.botLeft = botLeft;
            this.botRight = botRight;
            return this;
        }

        public Builder edges(
                UIRegion top,
                UIRegion bottom,
                UIRegion left,
                UIRegion right
        ) {
            this.top = top;
            this.bottom = bottom;
            this.left = left;
            this.right = right;
            return this;
        }

        public BoxSkin build() {
            return new BoxSkin(topLeft, topRight, botLeft, botRight, top, bottom, left, right, border);
        }
    }
}
