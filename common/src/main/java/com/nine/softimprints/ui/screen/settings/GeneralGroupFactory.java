package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.config.ModelContactFallbackPolicy;
import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.ui.component.list.GroupBuilder;
import com.nine.softimprints.ui.component.list.ListGroup;
import com.nine.softimprints.ui.util.constant.SIText;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import static com.nine.softimprints.ui.screen.settings.SettingsControls.booleanButton;
import static com.nine.softimprints.ui.screen.settings.SettingsControls.doubleSlider;

public class GeneralGroupFactory implements SettingsGroupFactory {

    private static void addCore(
            GroupBuildContext context,
            GroupBuilder builder
    ) {
        builder.widget(applyTooltip(
                booleanButton(context, "config.softimprints.group.general.enable_imprints", SIConfig.General.ENABLE_IMPRINTS),
                Component.translatable("config.softimprints.group.general.enable_imprints.tooltip")
        ));
        builder.rowWidgets(
                applyTooltip(
                        doubleSlider(context, "config.softimprints.group.general.step_distance", SIConfig.General.IMPRINT_STEP_DISTANCE, 0.01D, 2),
                        Component.translatable("config.softimprints.group.general.step_distance.tooltip")
                ),
                applyTooltip(
                        doubleSlider(context, "config.softimprints.group.general.rotation_step_degrees", SIConfig.General.IMPRINT_ROTATION_STEP_DEGREES, 1.0D, 0),
                        Component.translatable("config.softimprints.group.general.rotation_step_degrees.tooltip")
                )
        );
        builder.spacer(4);
    }

    private static void addModelContact(
            GroupBuildContext context,
            GroupBuilder builder
    ) {
        builder.section(Component.translatable("config.softimprints.group.general.section.model_contact"));
        builder.widget(modelContactControl(context));
        builder.widget(fallbackPolicyControl(context));
        builder.rowWidgets(
                applyTooltip(
                        doubleSlider(context, "config.softimprints.group.general.contact_band_height", SIConfig.General.IMPRINT_CONTACT_BAND_HEIGHT, 0.01D, 2),
                        Component.translatable("config.softimprints.group.general.contact_band_height.tooltip")
                )
        );
        builder.spacer(4);
    }

    private static void addCompat(
            GroupBuildContext context,
            GroupBuilder builder
    ) {
        // moved to plugins
    }

    private static Button modelContactControl(GroupBuildContext context) {
        var draft = context.editorContext().config().draft(SIConfig.General.IMPRINT_MODEL_CONTACT);
        boolean current = Boolean.TRUE.equals(draft.getDraft());
        return Button.builder(modelContactText(current), button -> {
                    boolean next = !Boolean.TRUE.equals(draft.getDraft());
                    draft.setDraft(next);
                    button.setMessage(modelContactText(next));
                    button.setTooltip(Tooltip.create(modelContactTooltip(next)));
                })
                .bounds(0, 0, 1, SettingsControls.DEFAULT_HEIGHT)
                .tooltip(Tooltip.create(modelContactTooltip(current)))
                .build();
    }

    private static Component modelContactText(boolean enabled) {
        return Component.translatable("config.softimprints.group.general.model_contact", SIText.onOffState(enabled));
    }

    private static Component modelContactTooltip(boolean enabled) {
        return Component.translatable(enabled
                ? "config.softimprints.group.general.model_contact.tooltip.on"
                : "config.softimprints.group.general.model_contact.tooltip.off");
    }

    private static Button fallbackPolicyControl(GroupBuildContext context) {
        var draft = context.editorContext().config().draft(SIConfig.General.MODEL_CONTACT_FALLBACK_POLICY);
        ModelContactFallbackPolicy current = draft.getDraft();
        return Button.builder(fallbackPolicyText(current), button -> {
                    ModelContactFallbackPolicy next = nextFallbackPolicy(draft.getDraft());
                    draft.setDraft(next);
                    button.setMessage(fallbackPolicyText(next));
                    button.setTooltip(Tooltip.create(fallbackPolicyTooltip(next)));
                })
                .bounds(0, 0, 1, SettingsControls.DEFAULT_HEIGHT)
                .tooltip(Tooltip.create(fallbackPolicyTooltip(current)))
                .build();
    }

    private static Component fallbackPolicyText(ModelContactFallbackPolicy policy) {
        return Component.translatable(
                "config.softimprints.group.general.fallback_policy",
                Component.translatable("config.softimprints.group.general.fallback_policy." + policy.id())
        );
    }

    private static Component fallbackPolicyTooltip(ModelContactFallbackPolicy policy) {
        return Component.translatable("config.softimprints.group.general.fallback_policy." + policy.id() + ".tooltip");
    }

    private static ModelContactFallbackPolicy nextFallbackPolicy(ModelContactFallbackPolicy policy) {
        return switch (policy) {
            case SKIP -> ModelContactFallbackPolicy.BOUNDING_BOX;
            case BOUNDING_BOX -> ModelContactFallbackPolicy.LAST_SNAPSHOT;
            case LAST_SNAPSHOT -> ModelContactFallbackPolicy.SKIP;
        };
    }

    private static <T extends AbstractWidget> T applyTooltip(
            T widget,
            Component tooltip
    ) {
        widget.setTooltip(Tooltip.create(tooltip));
        return widget;
    }

    @Override
    public EditorGroup key() {
        return EditorGroup.GENERAL;
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

        addCore(context, builder);
        addModelContact(context, builder);
        addCompat(context, builder);

        return builder.build();
    }

}
