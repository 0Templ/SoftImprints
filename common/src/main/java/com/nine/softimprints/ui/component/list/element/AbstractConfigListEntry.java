package com.nine.softimprints.ui.component.list.element;

import com.nine.softimprints.ui.component.list.ConfigListWidget;
import com.nine.softimprints.ui.component.list.ListGroup;

public abstract class AbstractConfigListEntry implements ConfigListEntry {

    private ConfigListWidget list;
    private ListGroup group;
    private int x;
    private int y;
    private int width;
    private int height;
    private boolean focused;

    protected AbstractConfigListEntry(int height) {
        this.height = height;
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void updateEntryLayout(int x, int y, int width) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.onEntryResized(width);
    }

    @Override
    public void updateHeight(int height) {
        if (height != this.height) {
            this.height = height;
            this.requestListLayout();
        }
    }

    @Override
    public final void attachToList(ConfigListWidget list, ListGroup group) {
        this.list = list;
        this.group = group;
        this.onAttached();
        this.requestListLayout();
    }


    @Override
    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    @Override
    public boolean isFocused() {
        return focused;
    }

    protected void onEntryResized(int width) {
    }

    protected void onAttached() {
    }

    protected final void requestListLayout() {
        if (this.list != null) {
            this.list.invalidateLayout();
        }
    }

    protected final ConfigListWidget list() {
        return this.list;
    }

    protected final ListGroup group() {
        return this.group;
    }
}
