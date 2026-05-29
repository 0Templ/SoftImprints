package com.nine.softimprints.ui.util.constant;

import net.minecraft.network.chat.Component;

public class SIText {

    public static Component onOffState(boolean val) {
        return val ? Component.translatable("options.on")
                : Component.translatable("options.off");
    }

}
