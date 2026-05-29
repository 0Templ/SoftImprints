package com.nine.softimprints.profile.catalog.entry.issue;

import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public record UnsupportedSchemaIssue(
        int foundVersion,
        int supportedVersion
) implements ProfileIssue {


    @Override
    public Component title() {
        return Component.translatable("imprint_profile.issue.unsupported_schema_version.title");
    }

    @Override
    public Component tooltip() {
        return Component.translatable("imprint_profile.issue.unsupported_schema_version.tooltip");
    }

    @Override
    public List<Component> details() {
        List<Component> ret = new ArrayList<>();
        ret.add(Component.translatable("imprint_profile.issue.unsupported_schema_version.details_0",
                Component.literal(String.valueOf(foundVersion)).withColor(SIColors.SOFT_SOFT_GRAY),
                Component.literal(String.valueOf(supportedVersion)).withColor(SIColors.SOFT_SOFT_GRAY)
        ));
        ret.add(Component.translatable("imprint_profile.issue.unsupported_schema_version.details_1"));
        return ret;
    }
}