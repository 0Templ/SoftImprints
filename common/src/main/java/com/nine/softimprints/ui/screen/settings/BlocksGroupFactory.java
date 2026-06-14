package com.nine.softimprints.ui.screen.settings;

import com.nine.softimprints.profile.catalog.entry.InvalidProfileEntry;
import com.nine.softimprints.profile.options.block.SurfaceBlock;
import com.nine.softimprints.ui.component.list.GroupBuilder;
import com.nine.softimprints.ui.component.list.ListGroup;
import com.nine.softimprints.ui.component.search.BlockSearchEntry;
import com.nine.softimprints.ui.component.search.SearchListBuilder;
import com.nine.softimprints.ui.component.search.SearchListEntry;
import com.nine.softimprints.ui.context.EditorContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class BlocksGroupFactory implements SettingsGroupFactory {

    private static void applySelection(
            EditorContext editor,
            Set<Block> selected
    ) {
        Set<SurfaceBlock> next = new HashSet<>();
        for (Block block : selected) {
            next.add(SurfaceBlock.of(block));
        }
        var draft = editor.currentDraft();
        if (draft == null) return;
        draft.updateDraft(profile ->
                profile.toBuilder().setSupportedBlocks(next).build()
        );
    }

    private static boolean hasNewSurfaceBlocks(EditorContext editor) {
        var draft = editor.currentDraft();
        if (draft == null) return false;
        Set<Identifier> currentIds = draft
                .getCurrent()
                .supportedBlocks()
                .stream()
                .map(SurfaceBlock::id)
                .collect(Collectors.toSet());

        return draft.getDraft()
                .supportedBlocks()
                .stream()
                .map(SurfaceBlock::id)
                .anyMatch(id -> !currentIds.contains(id));
    }

    @Override
    public EditorGroup key() {
        return EditorGroup.BLOCKS;
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
        EditorContext editor = context.editorContext();

        var builder = GroupBuilder.of(label())
                .spacer(3);

        if (editor.currentEntry() instanceof InvalidProfileEntry entry) {
            builder.issueDetails(entry);
            return builder.build();
        }

        builder.sectionMarker(
                Component.translatable("config.softimprints.group.blocks.section"),
                Component.translatable("config.softimprints.group.blocks.section.tooltip"),
                Component.literal("⚠"),
                Component.translatable("config.softimprints.group.blocks.reload_required.tooltip"),
                () -> hasNewSurfaceBlocks(editor)
        );

        var draft = editor.currentDraft();
        if (draft == null) return builder.build();
        var profile = draft.getDraft();

        Set<Block> initiallySelected = profile.supportedBlocks().stream()
                .map(SurfaceBlock::block)
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toSet());

        SearchListEntry<Block> blockList = SearchListBuilder.<Block>create()
                .addAll(BuiltInRegistries.BLOCK.stream()
                        .map(block -> new BlockSearchEntry(block, editor))
                        .toList())
                .visibleRows(6)
                .selectInitial(initiallySelected)

                .onChange(selected -> applySelection(editor, selected))
                .build();

        builder.searchList(blockList);
        builder.spacer(10);
        return builder.build();
    }
}
