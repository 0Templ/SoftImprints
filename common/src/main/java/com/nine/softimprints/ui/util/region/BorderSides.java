package com.nine.softimprints.ui.util.region;

import java.util.EnumSet;

public enum BorderSides {

    TOP, BOTTOM, LEFT, RIGHT;

    public static final EnumSet<BorderSides> FULL = EnumSet.allOf(BorderSides.class);
    public static final EnumSet<BorderSides> NO_BOTTOM = EnumSet.of(TOP, LEFT, RIGHT);
    public static final EnumSet<BorderSides> NO_TOP = EnumSet.of(BOTTOM, LEFT, RIGHT);
}
