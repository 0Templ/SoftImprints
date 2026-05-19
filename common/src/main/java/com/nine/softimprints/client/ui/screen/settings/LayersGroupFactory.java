package com.nine.softimprints.client.ui.screen.settings;

import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.model.SurfaceMode;
import com.nine.softimprints.client.profile.options.layer.ImprintLayer;
import com.nine.softimprints.client.profile.options.surface.SurfaceSettings;
import com.nine.softimprints.client.profile.options.texture.ImprintTextures;
import com.nine.softimprints.client.ui.component.list.GroupBuilder;
import com.nine.softimprints.client.ui.component.list.ListGroup;
import com.nine.softimprints.client.ui.component.slider.ExtendedSlider;
import com.nine.softimprints.client.ui.util.constant.SIText;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static com.nine.softimprints.client.ui.screen.settings.SettingsControls.doubleSlider;

public class LayersGroupFactory implements SettingsGroupFactory {

    @Override
    public EditorGroup key() {
        return EditorGroup.LAYERS;
    }

    @Override
    public GroupZone zone() {
        return GroupZone.MIDDLE;
    }

    @Override
    public boolean rebuildOnProfileChange() {
        return true;
    }

    @Override
    public ListGroup build(GroupBuildContext context) {
        var profile = context.editorContext().currentProfile().getDraft();
        GroupBuilder builder = GroupBuilder.of(label())
                .spacer(3);

        builder.widget(textureSetButton(context, profile.textureSets()));

        addResolutionButtons(builder, context);

        addLayerTypeButtons(builder, context, profile.surface());

        builder.spacer(4);

        for (ImprintLayer layer : profile.getLayers()) {
            addLayerSection(builder, layer);
            if (layer.value() == Constants.BASIC_LAYER_BYTE) {
                addInitLayer(context, builder, layer);
            } else {
                addRegularLayer(context, builder, layer);
            }
        }

        return builder.build();
    }

    private static void addInitLayer(GroupBuildContext context, GroupBuilder builder, ImprintLayer layer) {
        builder.widget(expandSlider(context, layer));
        builder.rowWidgets(
                layerSlider("config.softimprints.group.layers.outer_jitter", layer.outerJitter(), v ->
                        updateLayer(context, layer.value(), l -> l.toBuilder().setOuterJitter(v.floatValue()).build())
                ),
                layerSlider("config.softimprints.group.layers.erosion", layer.erosion(), v ->
                        updateLayer(context, layer.value(), l -> l.toBuilder().setErosion(v.floatValue()).build())
                )
        );
        builder.spacer(4);
    }

    private static void addRegularLayer(GroupBuildContext context, GroupBuilder builder, ImprintLayer layer) {
        builder.widget(enableButton(context, layer));
        builder.rowWidgets(
                expandSlider(context, layer),
                layerSlider("config.softimprints.group.layers.erosion", layer.erosion(), v ->
                        updateLayer(context, layer.value(), l -> l.toBuilder().setErosion(v.floatValue()).build())
                )
        );
        builder.rowWidgets(
                layerSlider("config.softimprints.group.layers.inner_jitter", layer.innerJitter(), v ->
                        updateLayer(context, layer.value(), l -> l.toBuilder().setInnerJitter(v.floatValue()).build())
                ),
                layerSlider("config.softimprints.group.layers.outer_jitter", layer.outerJitter(), v ->
                        updateLayer(context, layer.value(), l -> l.toBuilder().setOuterJitter(v.floatValue()).build())
                )
        );
        builder.spacer(4);
    }

    private static void addLayerSection(GroupBuilder builder, ImprintLayer layer) {
        builder.section(Component.translatable("config.softimprints.group.layers.layer_section", layer.value()));
    }

    private static Button textureSetButton(GroupBuildContext context, ImprintTextures textureSets) {
        var currentId = context.editorContext().currentProfileId();
        Button button = Button.builder(textureSetText(currentId, textureSets), b ->
                context.editorContext().currentProfile().updateDraft(profile -> {
                    ImprintTextures next = profile.textureSets().selectNext();
                    b.setMessage(textureSetText(currentId, next));
                    return profile.toBuilder()
                            .setTextureSets(next)
                            .build();
                })
        ).build();
        button.setTooltip(Tooltip.create(Component.translatable("config.softimprints.group.layers.texture_set.tooltip")));
        button.active = textureSets.map().size() > 1;
        return button;
    }

    private static void addResolutionButtons(GroupBuilder builder, GroupBuildContext context) {

        var draftProfile = context.editorContext().currentProfile().getDraft();
        var currentRes = draftProfile.resolution();

        var mapResSlider = ExtendedSlider.builder("config.softimprints.group.layers.map_size")
                .bounds(0, 0, 1, 18)
                .range(1, 64)
                .step(1, 1)
                .value(currentRes.mapSize())
                .build()
                .addListener(v -> {
                            context.editorContext().currentProfile().updateDraft(profile -> {
                                return profile.toBuilder()
                                        .setResolution(profile.resolution().withMapSize(v.intValue()))
                                        .build();
                            });
                        }
                );
        mapResSlider.setTooltip(Tooltip.create(Component.translatable("config.softimprints.group.layers.map_size.tooltip")));

        builder.widget(mapResSlider);
    }

    private static Component textureSetText(Identifier currentProfile, ImprintTextures textureSets) {
        var setTrId = ("imprint_profile.") + currentProfile.toLanguageKey() + ("." + textureSets.selected());
        return Component.translatable("config.softimprints.group.layers.texture_set",
                Component.translatable(setTrId)
        );
    }

    private static void addLayerTypeButtons(GroupBuilder builder, GroupBuildContext context, SurfaceSettings surface) {

        var zeroLayerButton = Button.builder(zeroLayerSourceText(surface), b ->
                context.editorContext().currentProfile().updateDraft(profile -> {
                    SurfaceSettings next = profile.surface().toggleZeroLayerSource();
                    b.setMessage(zeroLayerSourceText(next));
                    b.setTooltip(Tooltip.create(zeroLayerSourceTooltip(next)));
                    return profile.toBuilder()
                            .setSurface(next)
                            .build();
                })
        ).tooltip(Tooltip.create(zeroLayerSourceTooltip(surface))).build();

        zeroLayerButton.setTooltip(Tooltip.create(zeroLayerSourceTooltip(surface)));
        zeroLayerButton.active = surface.mode() == SurfaceMode.REPAINT;

        var surfaceModeButton = Button.builder(surfaceModeText(surface), b ->
                context.editorContext().currentProfile().updateDraft(profile -> {
                    var current = profile.surface().mode();
                    SurfaceSettings next = profile.surface().withMode(
                            current == SurfaceMode.REPAINT ? SurfaceMode.OVERLAY : SurfaceMode.REPAINT
                    );
                    zeroLayerButton.active = next.mode() == SurfaceMode.REPAINT;
                    b.setMessage(surfaceModeText(next));
                    b.setTooltip(Tooltip.create(surfaceModeTooltip(next)));

                    return profile.toBuilder()
                            .setSurface(next)
                            .build();
                })
        ).tooltip(Tooltip.create(surfaceModeTooltip(surface))).build();

        builder.rowWidgets(
                surfaceModeButton,
                zeroLayerButton
        );
    }

    private static Component surfaceModeText(SurfaceSettings surface) {
        var type = surface.mode().toString().toLowerCase();
        return Component.translatable(
                "config.softimprints.group.layers.emit_strategy",
                Component.translatable("config.softimprints.group.layers.emit_strategy." + type)
        );
    }

    private static Component surfaceModeTooltip(SurfaceSettings surface) {
        var type = surface.mode().toString().toLowerCase();
        return Component.translatable("config.softimprints.group.layers.emit_strategy." + type + ".tooltip");
    }

    private static Component zeroLayerSourceTooltip(SurfaceSettings surface) {
        return surface.useOriginalZeroLayer() ?
                Component.translatable("config.softimprints.group.layers.zero_layer_source.original.tooltip") :
                Component.translatable("config.softimprints.group.layers.zero_layer_source.profile.tooltip");
    }

    private static Component zeroLayerSourceText(SurfaceSettings surface) {
        return Component.translatable(
                "config.softimprints.group.layers.zero_layer_source",
                Component.translatable(surface.useOriginalZeroLayer()
                        ? "config.softimprints.group.layers.zero_layer_source.original"
                        : "config.softimprints.group.layers.zero_layer_source.priority")
        );
    }

    private static ExtendedSlider expandSlider(GroupBuildContext context, ImprintLayer layer) {
        return applyTooltip(ExtendedSlider.builder("config.softimprints.group.layers.expand")
                .bounds(0, 0, 1, 18)
                .range(0, 8)
                .value(layer.expand())
                .build()
                .addListener(v -> updateLayer(context, layer.value(), l ->
                        l.toBuilder().setExpand(v.intValue()).build()
                )), Component.translatable("config.softimprints.group.layers.expand.tooltip"));
    }

    private static Button enableButton(GroupBuildContext context, ImprintLayer layer) {
        return Button.builder(
                enabledText(layer.enable()),
                b -> {
                    updateLayer(context, layer.value(), l -> {
                        b.setMessage(enabledText(!l.enable()));
                        return l.toBuilder().setEnabled(!l.enable()).build();
                    });
                }).build();
    }

    private static Component enabledText(boolean enabled) {
        return Component.translatable("config.softimprints.group.layers.layer_enabled", SIText.onOffState(enabled));
    }

    private static ExtendedSlider layerSlider(String labelKey, double value, Consumer<Double> listener) {
        return applyTooltip(
                doubleSlider(labelKey, 0.0D, 1.0D, value, 0.01D, 2, listener),
                Component.translatable(labelKey + ".tooltip")
        );
    }

    private static void updateLayer(
            GroupBuildContext context,
            byte value,
            UnaryOperator<ImprintLayer> updater
    ) {
        context.editorContext().currentProfile().updateDraft(profile ->
                profile.toBuilder()
                        .mutateLayer(value, updater)
                        .build()
        );
    }

    private static <T extends AbstractWidget> T applyTooltip(T widget, Component tooltip) {
        widget.setTooltip(Tooltip.create(tooltip));
        return widget;
    }

}
