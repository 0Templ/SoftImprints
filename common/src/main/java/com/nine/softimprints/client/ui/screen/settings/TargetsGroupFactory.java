package com.nine.softimprints.client.ui.screen.settings;

import com.nine.softimprints.client.config.EntityTargetFilterMode;
import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.ui.cache.UICache;
import com.nine.softimprints.client.ui.component.list.GroupBuilder;
import com.nine.softimprints.client.ui.component.list.ListGroup;
import com.nine.softimprints.client.ui.component.search.EntitySearchEntry;
import com.nine.softimprints.client.ui.component.search.SearchListBuilder;
import com.nine.softimprints.client.ui.component.search.SearchListEntry;
import com.nine.softimprints.client.ui.component.search.SearchListMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class TargetsGroupFactory implements SettingsGroupFactory {

    @Override
    public EditorGroup key() {
        return EditorGroup.TARGETS;
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
        var builder = GroupBuilder.of(label())
                .spacer(3);
        builder.section(Component.translatable("config.softimprints.group.targets.section"));

        var config = context.editorContext().config();
        EntityTargetFilterMode mode = config.draftValue(SIConfig.Targets.IMPRINT_TARGET_FILTER_MODE);
        Set<EntityType<?>> blacklist = readEntitySet(config.draftValue(SIConfig.Targets.IMPRINT_TARGET_BLACKLIST));
        Set<EntityType<?>> whitelist = readEntitySet(config.draftValue(SIConfig.Targets.IMPRINT_TARGET_WHITELIST));

        SearchListEntry<EntityType<?>> entityList = SearchListBuilder.<EntityType<?>>create()
                .addAll(BuiltInRegistries.ENTITY_TYPE.stream()
                        .map(EntitySearchEntry::new)
                        .toList())
                .visibleRows(6)
                .addMode(SearchListMode.of(
                        EntityTargetFilterMode.BLACKLIST.id(),
                        Component.translatable("config.softimprints.group.targets.mode.blacklist"),
                        blacklist,
                        selected -> config.setDraft(SIConfig.Targets.IMPRINT_TARGET_BLACKLIST, writeEntitySet(selected))
                ))
                .addMode(SearchListMode.of(
                        EntityTargetFilterMode.WHITELIST.id(),
                        Component.translatable("config.softimprints.group.targets.mode.whitelist"),
                        whitelist,
                        selected -> config.setDraft(SIConfig.Targets.IMPRINT_TARGET_WHITELIST, writeEntitySet(selected))
                ))
                .initialMode(mode.id())
                .onModeChanged(modeId -> config.setDraft(SIConfig.Targets.IMPRINT_TARGET_FILTER_MODE, EntityTargetFilterMode.fromId(modeId)))
                .build();

        builder.searchList(entityList);
        builder.spacer(10);
        return builder.build();
    }

    private static Set<EntityType<?>> readEntitySet(List<String> ids) {
        Set<EntityType<?>> result = new LinkedHashSet<>();
        for (String raw : ids) {
            Identifier id = Identifier.tryParse(raw);
            if (id == null) continue;
            BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresent(result::add);
        }
        return result;
    }

    private static List<String> writeEntitySet(Set<EntityType<?>> entities) {
        List<String> result = new ArrayList<>(entities.size());
        for (EntityType<?> type : entities) {
            result.add(BuiltInRegistries.ENTITY_TYPE.getKey(type).toString());
        }
        return result;
    }

}
