package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.api.meta.distribution.Distribution;
import com.nine.softimprints.api.meta.update.SIUpdateCandidate;
import com.nine.softimprints.api.meta.update.SIUpdateService;
import com.nine.softimprints.core.Constants;
import com.nine.softimprints.platform.Platform;
import com.nine.softimprints.ui.component.list.GroupBuilder;
import com.nine.softimprints.ui.component.list.ListGroup;
import com.nine.softimprints.ui.component.widget.LabelWidget;
import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.Map;

public class InfoGroupFactory implements SettingsGroupFactory {

    public static void openLinkPrompt(String url) {
        Minecraft minecraft = Minecraft.getInstance();
        Screen previous = minecraft.screen;
        minecraft.setScreen(new ConfirmLinkScreen(
                confirmed -> {
                    if (confirmed) {
                        Util.getPlatform().openUri(url);
                    }
                    minecraft.setScreen(previous);
                },
                url,
                true
        ));
    }

    @Override
    public EditorGroup key() {
        return EditorGroup.INFO;
    }

    @Override
    public GroupZone zone() {
        return GroupZone.RIGHT;
    }

    @Override
    public boolean rebuildOnProfileChange() {
        return false;
    }

    @Override
    public ListGroup build(GroupBuildContext context) {
        GroupBuilder builder = GroupBuilder.of(label())
                .spacer(1);
        Font font = Minecraft.getInstance().font;
        var currentVersion = Platform.CORE.modVersion();

        builder.label(Component.literal("Mod Info").withStyle(ChatFormatting.UNDERLINE));


        addMetaInfo(builder, font);

//        builder.spacer(2);
        addUpdateInfo(builder, font);

        builder.spacer(7);
        addGithibInfo(builder, font);

        builder.spacer(2);

        return builder.build();
    }

    private void addMetaInfo(
            GroupBuilder builder,
            Font font
    ) {
        var currentVersion = Platform.CORE.modVersion();
        builder.spacer(1);
        builder.rowLabels(font, LabelWidget.singleLine(Component.translatable("config.softimprints.group.info.meta.version", currentVersion)));
        builder.height(9);
    }

    private void addUpdateInfo(
            GroupBuilder builder,
            Font font
    ) {
        if (SIUpdateService.hasUpdate()) {
            var currentVersion = Platform.CORE.modVersion();
            var result = SIUpdateService.current();
            if (result != null) {
                var candidates = result.candidates();
                if (candidates != null) {
                    var firstValid = candidates.entrySet().stream().findFirst();
                    String updVersion = "???";
                    if (firstValid.isPresent()) {
                        updVersion = firstValid.get().getValue().version();
                    }
                    builder.rowLabels(font,
                            LabelWidget.singleLine(Component.translatable("Update Available")),
                            LabelWidget.singleLine(Component.literal("(")),
                            LabelWidget.singleLine(Component.literal(currentVersion)),
                            LabelWidget.singleLine(Component.literal("→")),
                            LabelWidget.singleLine(Component.literal(updVersion),
                                    SIColors.SOFT_GRAY, SIColors.ALMOST_WHITE
                            ),
                            LabelWidget.singleLine(Component.literal(")"))
                    );
                    builder.height(9);

                    var modrinth = buildDistroWidget(candidates, Distribution.MODRINTH, 0XFF55c684, 0XFF1bd96a);
                    var curseForge = buildDistroWidget(candidates, Distribution.CURSEFORGE, 0XFFd17f5e, 0XFFe26a39);

                    builder.rowLabels(font,
                            LabelWidget.singleLine(Component.literal("[")),
                            modrinth.widget,
                            LabelWidget.singleLine(Component.literal("/")),
                            curseForge.widget,
                            LabelWidget.singleLine(Component.literal("]"))
                    );
                    builder.height(9);
                }
            }
        } else {
            builder.label(Component.translatable("config.softimprints.group.info.update.up_to_date"));
        }
    }

    private DistributionWidget buildDistroWidget(
            Map<Distribution, SIUpdateCandidate> candidates,
            Distribution distribution,
            int color,
            int colorHovered
    ) {
        var candidate = candidates.get(distribution);
        boolean valid = candidate != null;
        LabelWidget ret;
        if (valid) {
            ret = LabelWidget.singleLine(Component.translatable(distribution.getLabelKey()),
                    color, colorHovered, () -> {
                        openLinkPrompt(candidate.url());
                    });
            ret.setTooltip(Tooltip.create(Component.translatable("config.softimprints.group.info.update.download.tooltip",
                    candidate.url())));
        } else {
            ret = LabelWidget.singleLine(Component.translatable(distribution.getLabelKey()),
                    0Xff6e6e6e, 0Xff56645b);
            ret.setTooltip(Tooltip.create(Component.translatable("config.softimprints.group.info.update.not_found")));
        }
        return new DistributionWidget(valid, ret);
    }

    private void addGithibInfo(
            GroupBuilder builder,
            Font font
    ) {
        builder.rowLabels(font,
                LabelWidget.singleLine(Component.translatable("config.softimprints.group.info.meta.report_a_bug").withStyle(ChatFormatting.UNDERLINE),
                        () -> {
                            openLinkPrompt(Constants.ISSUES_LINK);
                        })
        );
        builder.height(9);
        builder.rowLabels(font,
                LabelWidget.singleLine(Component.translatable("config.softimprints.group.info.meta.create_pack").withStyle(ChatFormatting.UNDERLINE),
                        () -> {
                            openLinkPrompt(Constants.GITHUB_HOME_PAGE);
                        })
        );
        builder.height(9);
    }

    private record DistributionWidget(boolean valid, LabelWidget widget) {
    }

}
