package com.nine.softimprints.core.contact.model;

import net.minecraft.client.renderer.rendertype.RenderType;

import java.util.Locale;

public final class ModelContactRenderTypes {

    private ModelContactRenderTypes() {
    }

    public static boolean shouldCapture(RenderType renderType) {
        if (true) return true; //todo: tests
        if (renderType == null) {
            return false;
        }

        String name = renderType.toString().toLowerCase(Locale.ROOT);
        return !isDecorativeEntityLayer(name);
    }

    private static boolean isDecorativeEntityLayer(String name) {
        return name.contains("eyes")
                || name.contains("emissive")
                || name.contains("glint")
                || name.contains("outline")
                || name.contains("energy_swirl")
                || name.contains("dragon_rays")
                || name.contains("leash")
                || name.contains("lightning");
    }
}
