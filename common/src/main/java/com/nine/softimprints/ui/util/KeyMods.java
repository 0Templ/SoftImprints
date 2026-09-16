package com.nine.softimprints.ui.util;

import com.mojang.blaze3d.platform.InputConstants;

public final class KeyMods {

    public static boolean shiftPressed() {
        return InputConstants.isKeyDown(InputConstants.KEY_LSHIFT)
                || InputConstants.isKeyDown(InputConstants.KEY_RSHIFT);
    }

    public static boolean altPressed() {
        return InputConstants.isKeyDown(InputConstants.KEY_LALT)
                || InputConstants.isKeyDown(InputConstants.KEY_RALT);
    }


}
