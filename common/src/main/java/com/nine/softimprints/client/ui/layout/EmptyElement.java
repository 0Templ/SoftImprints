package com.nine.softimprints.client.ui.layout;

public final class EmptyElement implements UILayoutElement {

    public static final EmptyElement INSTANCE = new EmptyElement();

    private EmptyElement() {
    }

    @Override
    public void place(LayoutRect bounds) {
    }
}
