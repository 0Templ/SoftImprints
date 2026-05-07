package com.nine.softimprints.client.ui.screen.settings;

import com.nine.softimprints.client.config.ConfigValue;
import com.nine.softimprints.client.ui.component.slider.ExtendedSlider;
import com.nine.softimprints.client.ui.context.ConfigSession;
import com.nine.softimprints.client.ui.draft.DraftHolder;
import com.nine.softimprints.client.ui.util.constant.SIText;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

final class SettingsControls {

    static final int DEFAULT_HEIGHT = 20;

    private SettingsControls() {
    }

    static Button booleanButton(GroupBuildContext context, String labelKey, ConfigValue<Boolean> config) {
        DraftHolder<Boolean> draft = context.editorContext().config().draft(config);
        return Button.builder(booleanText(labelKey, draft.getDraft()), button -> {
                    boolean next = !Boolean.TRUE.equals(draft.getDraft());
                    draft.setDraft(next);
                    button.setMessage(booleanText(labelKey, next));
                })
                .bounds(0, 0, 1, DEFAULT_HEIGHT)
                .build();
    }

    static <T> Button cyclingButton(
            GroupBuildContext context,
            ConfigValue<T> config,
            UnaryOperator<T> nextValue,
            Function<T, Component> message
    ) {
        DraftHolder<T> draft = context.editorContext().config().draft(config);
        return Button.builder(message.apply(draft.getDraft()), button -> {
                    T next = nextValue.apply(draft.getDraft());
                    draft.setDraft(next);
                    button.setMessage(message.apply(next));
                })
                .bounds(0, 0, 1, DEFAULT_HEIGHT)
                .build();
    }

    public static ExtendedSlider intSlider(GroupBuildContext context, String labelKey, ConfigValue<Integer> config) {
        ConfigSession configSession = context.editorContext().config();
        ExtendedSlider slider = slider(
                labelKey,
                config.min().doubleValue(),
                config.max().doubleValue(),
                configSession.draftValue(config),
                1.0D,
                0
        );
        slider.addListener(value -> configSession.setDraft(config, Math.toIntExact(Math.round(value))));
        return slider;
    }

    public static ExtendedSlider doubleSlider (
            GroupBuildContext context,
            String labelKey,
            ConfigValue<Double> config,
            double step,
            int precision
    ) {
        ConfigSession configSession = context.editorContext().config();
        ExtendedSlider slider = slider(
                labelKey,
                config.min().doubleValue(),
                config.max().doubleValue(),
                configSession.draftValue(config),
                step,
                precision
        );
        slider.addListener(value -> configSession.setDraft(config, value));
        return slider;
    }

    public static ExtendedSlider doubleSlider(
            String labelKey,
            double min,
            double max,
            double value,
            double step,
            int precision,
            Consumer<Double> listener
    ) {
        ExtendedSlider slider = slider(labelKey, min, max, value, step, precision);
        slider.addListener(listener);
        return slider;
    }

    private static ExtendedSlider slider(
            String labelKey,
            double min,
            double max,
            double value,
            double step,
            int precision
    ) {
        return ExtendedSlider.builder(labelKey)
                .bounds(0, 0, 1, DEFAULT_HEIGHT)
                .range(min, max)
                .value(value)
                .step(step, precision)
                .build();
    }

    private static Component booleanText(String labelKey, boolean value) {
        return Component.translatable(labelKey, SIText.onOffState(value));
    }


}
