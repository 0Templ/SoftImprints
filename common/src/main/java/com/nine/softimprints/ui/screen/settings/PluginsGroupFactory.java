package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.api.plugin.ImprintPluginInfo;
import com.nine.softimprints.api.plugin.ImprintPlugins;
import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.ui.component.list.GroupBuilder;
import com.nine.softimprints.ui.component.list.ListGroup;
import com.nine.softimprints.ui.context.ConfigSession;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashSet;
import java.util.List;

import static com.nine.softimprints.ui.screen.settings.SettingsControls.DEFAULT_HEIGHT;

public class PluginsGroupFactory implements SettingsGroupFactory {

    @Override
    public EditorGroup key() {
        return EditorGroup.PLUGINS;
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
                .spacer(4);
        var config = context.editorContext().config();
        var plugins = ImprintPlugins.pluginsMeta();

        builder.label(Component.translatable("config.softimprints.group.plugins.section.installed"));
        builder.spacer(2);

        if (plugins.isEmpty()) {
            builder.label(Component.translatable("config.softimprints.group.plugins.empty"));
        } else {
            plugins.forEach(plugin -> {
                builder.widget(pluginButton(plugin, config));
            });
        }

        return builder.build();
    }

    private Button pluginButton(
            ImprintPluginInfo plugin,
            ConfigSession config
    ) {
        var idStr = plugin.id().toString();
        var disabledPlugins = SIConfig.Plugins.DISABLED_PLUGINS;
        var ret = Button.builder(plugin.title().apply(pluginEnabledFromDraft(config.draftValue(disabledPlugins), idStr)), button -> {
                    var holder = config.draft(disabledPlugins);
                    boolean has = holder.getDraft().contains(idStr);
                    var toSet = new LinkedHashSet<>(holder.getDraft());
                    if (has) {
                        toSet.remove(idStr);
                    } else {
                        toSet.add(idStr);
                    }
                    List<String> nextDraft = List.copyOf(toSet);
                    config.setDraft(disabledPlugins, nextDraft);
                    button.setMessage(plugin.title().apply(pluginEnabledFromDraft(nextDraft, idStr)));
                })
                .bounds(0, 0, 1, DEFAULT_HEIGHT)
                .build();

        ret.setTooltip(Tooltip.create(plugin.tooltip()));

        return ret;
    }

    private boolean pluginEnabledFromDraft(
            List<String> list,
            String id
    ) {
        return !list.contains(id);
    }

}
