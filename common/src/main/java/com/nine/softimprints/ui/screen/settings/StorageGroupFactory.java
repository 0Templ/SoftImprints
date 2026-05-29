package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.config.ConfigValue;
import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.ui.component.list.GroupBuilder;
import com.nine.softimprints.ui.component.list.ListGroup;
import com.nine.softimprints.ui.component.slider.ExtendedSlider;
import com.nine.softimprints.ui.context.ConfigSession;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.function.Function;

import static com.nine.softimprints.ui.screen.settings.SettingsControls.intSlider;


public class StorageGroupFactory implements SettingsGroupFactory {

    @Override
    public EditorGroup key() {
        return EditorGroup.STORAGE;
    }

    @Override
    public GroupZone zone() {
        return GroupZone.LEFT;
    }

    @Override
    public boolean rebuildOnProfileChange() {
        return false;
    }

    @Override
    public ListGroup build(GroupBuildContext context) {
        GroupBuilder builder = GroupBuilder.of(label())
                .spacer(4);

        builder.label(Component.translatable("config.softimprints.group.storage.section.entities"));
        var maxTrackEntitiesSlider = maxAsUnlimitedSlider(
                context, "config.softimprints.group.storage.max_track_entities",
                SIConfig.Performance.MAX_TRACK_ENTITIES);
        applyTooltip(maxTrackEntitiesSlider, Component.translatable("config.softimprints.group.storage.max_track_entities.tooltip"));
        builder.widget(maxTrackEntitiesSlider);

        var trackRangeSlider = maxAsUnlimitedSlider(
                context, "config.softimprints.group.storage.max_track_range",
                SIConfig.Performance.ENTITIES_TRACK_RANGE);
        applyTooltip(trackRangeSlider, Component.translatable("config.softimprints.group.storage.max_track_range.tooltip"));
        builder.widget(trackRangeSlider);

        builder.spacer(2);

        builder.label(Component.translatable("config.softimprints.group.storage.section.model_capture"));
        builder.widget(applyTooltip(
                intSlider(context, "config.softimprints.group.storage.snapshot_interval", SIConfig.Performance.IMPRINT_SNAPSHOT_INTERVAL),
                Component.translatable("config.softimprints.group.storage.snapshot_interval.tooltip")
        ));



        builder.spacer(2);

        builder.label(Component.translatable("config.softimprints.group.storage.section.cache"));
        builder.widget(applyTooltip(
                SettingsControls.booleanButton(context, "config.softimprints.group.storage.clear_on_chunk_unload", SIConfig.Performance.CLEAR_IMPRINTS_ON_CHUNK_UNLOAD),
                Component.translatable("config.softimprints.group.storage.clear_on_chunk_unload.tooltip")
        ));

        var blocksLimSlider = maxAsUnlimitedSlider(
                context,
                "config.softimprints.group.storage.max_cached_imprint_blocks",
                SIConfig.Performance.MAX_CACHED_IMPRINT_BLOCKS,
                128.0D,
                0
        );
        applyTooltip(blocksLimSlider, Component.translatable("config.softimprints.group.storage.max_cached_imprint_blocks.tooltip"));
        builder.widget(blocksLimSlider);

        builder.spacer(2);
        builder.label(Component.translatable("config.softimprints.group.storage.section.world_updates"));
        var maxSectionsRebuildSlider = maxAsUnlimitedSlider(
                context, "config.softimprints.group.storage.max_sections_rebuild",
                SIConfig.Performance.MAX_SECTIONS_REBUILD_PER_ITERATION);
        applyTooltip(maxSectionsRebuildSlider, Component.translatable("config.softimprints.group.storage.max_sections_rebuild.tooltip"));
        builder.widget(maxSectionsRebuildSlider);

        builder.rowWidgets(
                applyTooltip(
                        SettingsControls.intSlider(context, "config.softimprints.group.storage.write_check_tickrate", SIConfig.Performance.IMPRINT_WRITE_CHECK_TICK_RATE),
                        Component.translatable("config.softimprints.group.storage.write_check_tickrate.tooltip")
                ),
                applyTooltip(
                        SettingsControls.intSlider(context, "config.softimprints.group.storage.chunk_rebuild_tickrate", SIConfig.Performance.IMPRINT_CHUNK_REBUILD_TICK_RATE),
                        Component.translatable("config.softimprints.group.storage.chunk_rebuild_tickrate.tooltip"))
        );

        builder.spacer(2);


//                .row(
//                        RowItem.widget(1, button("Discard Draft", _ -> context.editorContext().currentProfile().discardDraft())),
//                        RowItem.widget(1, button("Restore Default", _ -> context.editorContext().currentProfile().restoreDefaultDraft()))
//                )
//                .widget(button("Apply Current Draft", _ -> context.editorContext().currentProfile().applyDraft()))
//                .spacer(4)
//                .label(Component.literal("This group is cached and does not rebuild on priority switch."))

        return builder.build();
    }


    private static ExtendedSlider maxAsUnlimitedSlider(
            GroupBuildContext context,
            String labelKey,
            ConfigValue<Integer> config
    ) {
        return maxAsUnlimitedSlider(context, labelKey, config, 1.0D, 0);
    }

    private static ExtendedSlider maxAsUnlimitedSlider(
            GroupBuildContext context,
            String labelKey,
            ConfigValue<Integer> config,
            double stepSize,
            int precision
    ) {
        return configSlider(context, labelKey, config, value -> {
            int intValue = Math.toIntExact(Math.round(value));
            return intValue == config.max().intValue()
                    ? Component.translatable("config.softimprints.unlimited")
                    : (intValue == 0 ? Component.translatable("config.softimprints.disabled"):
                    Component.literal(String.valueOf(intValue)));
        }, stepSize, precision);
    }

    private static ExtendedSlider configSlider(
            GroupBuildContext context,
            String labelKey,
            ConfigValue<Integer> config,
            Function<Double, Component> valueFormatter,
            double stepSize,
            int precision
    ) {
        ConfigSession configSession = context.editorContext().config();
        return ExtendedSlider.builder(labelKey)
                .bounds(0, 0, 1, SettingsControls.DEFAULT_HEIGHT)
                .range(config.min().doubleValue(), config.max().doubleValue())
                .value(configSession.draftValue(config))
                .step(stepSize, precision)
                .valueFormatter(valueFormatter)
                .build()
                .addListener(value -> configSession.setDraft(config, Math.toIntExact(Math.round(value))));
    }

    private static <T extends AbstractWidget> T applyTooltip(T widget, Component tooltip) {
        widget.setTooltip(Tooltip.create(tooltip));
        return widget;
    }

}
