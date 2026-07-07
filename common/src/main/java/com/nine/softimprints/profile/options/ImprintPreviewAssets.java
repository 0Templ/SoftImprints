package com.nine.softimprints.profile.options;

import net.minecraft.resources.Identifier;

import javax.annotation.Nullable;

public record ImprintPreviewAssets(
        Identifier base,
        Identifier icon,
        @Nullable Identifier landingSound
) {
}
