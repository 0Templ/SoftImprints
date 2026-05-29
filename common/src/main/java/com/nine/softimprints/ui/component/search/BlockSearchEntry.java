package com.nine.softimprints.ui.component.search;

import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.options.block.SurfaceBlock;
import com.nine.softimprints.profile.resolver.ProfileCandidate;
import com.nine.softimprints.ui.context.EditorContext;
import com.nine.softimprints.ui.util.constant.SIColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Objects;

public final class BlockSearchEntry implements SearchableEntry<Block> {

    private static final Component CONFLICT_MARKER = Component.literal("\u26A0");
    private static final int CONFLICT_MARKER_WIDTH = 10;
    private static final int TOOLTIP_MAX_WIDTH = 180;

    private final Block block;
    private final String displayName;
    private final String registryKey;
    private final Item item;
    private final EditorContext editorContext;

    private ItemStack iconStack = ItemStack.EMPTY;
    private boolean iconResolved = false;

    public BlockSearchEntry(Block block, EditorContext editorContext) {
        this.block = block;
        this.editorContext = editorContext;
        this.item = block.asItem();
        this.displayName = block.getName().getString();
        this.registryKey = BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    @Override
    public Block value() {
        return block;
    }

    @Override
    public boolean matches(String lowercaseQuery) {
        return displayName.toLowerCase().contains(lowercaseQuery)
                || registryKey.toLowerCase().contains(lowercaseQuery);
    }

    @Override
    public void render(
            GuiGraphicsExtractor graphics,
            int x, int y, int width, int height,
            int mouseX, int mouseY, float partialTick,
            boolean hovered, boolean selected
    ) {
        int rowX = x;
        int rowWidth = width;
        Component conflictTooltip = item instanceof BlockItem blockItem
                ? conflictTooltip(blockItem.getBlock())
                : null;
        if (conflictTooltip != null) {
            Font font = Minecraft.getInstance().font;
            int markerY = y + (height - font.lineHeight) / 2;
            graphics.text(font, CONFLICT_MARKER, x, markerY, SIColors.YELLOW);
            if (mouseX >= x && mouseX < x + font.width(CONFLICT_MARKER)
                    && mouseY >= y && mouseY < y + height) {
                graphics.setTooltipForNextFrame(
                        font,
                        font.split(conflictTooltip, TOOLTIP_MAX_WIDTH),
                        mouseX,
                        mouseY
                );
            }

            rowX += 12;
            rowWidth -= 12;
        }

        IconNameRowRenderer.render(
                graphics, rowX, y, rowWidth, height, mouseX, mouseY,
                resolveIcon(), displayName, hovered, selected
        );
    }

    private Component conflictTooltip(Block block) {
        ImprintProfile current = editorContext.requireCurrentDraft().getDraft();
        if (!supports(current, block)) {
            return null;
        }

        List<ProfileCandidate> others = ImprintProfiles.profileCandidates(block).stream()
                .filter(candidate -> !Objects.equals(candidate.profileId(), current.id()))
                .toList();
        if (others.isEmpty()) {
            return null;
        }

        int maxOtherPriority = others.stream()
                .mapToInt(ProfileCandidate::priority)
                .max()
                .orElse(Integer.MIN_VALUE);

        MutableComponent otherNames = joinCandidateNames(others).withColor(SIColors.ALMOST_WHITE);
        Component currentName = profileName(current.id());

        if (current.priority() > maxOtherPriority) {
            return Component.translatable(
                    "config.softimprints.group.blocks.priority_conflict.current_wins.tooltip",
                    currentName,
                    otherNames
            );
        }

        return Component.translatable(
                "config.softimprints.group.blocks.priority_conflict.current_loses.tooltip",
                otherNames
        );
    }

    private static boolean supports(ImprintProfile profile, Block block) {
        return profile.supportedBlocks().stream()
                .map(SurfaceBlock::block)
                .filter(Objects::nonNull)
                .anyMatch(block::equals);
    }

    private static MutableComponent joinCandidateNames(List<ProfileCandidate> candidates) {
        MutableComponent ret = Component.empty();
        for (int i = 0; i < candidates.size(); i++) {
            if (i > 0) {
                ret.append(Component.literal(", "));
            }
            ret.append(candidateName(candidates.get(i)));
        }
        return ret;
    }

    private static Component candidateName(ProfileCandidate candidate) {
        Identifier profileId = candidate.profileId();
        if (profileId != null) {
            return profileName(profileId);
        }
        Identifier pluginId = candidate.pluginId();
        return pluginId == null
                ? Component.literal("Plugin resolver")
                : Component.literal(pluginId.toString());
    }

    private static Component profileName(Identifier id) {
        return Component.translatable("imprint_profile." + id.toLanguageKey());
    }

    private ItemStack resolveIcon() {
        if (iconResolved) return iconStack;
        if (item == Items.AIR) {
            iconStack = ItemStack.EMPTY;
            iconResolved = true;
            return iconStack;
        }
        try {
            iconStack = item.getDefaultInstance();
            iconResolved = true;
        } catch (Exception ignored) {
            iconStack = ItemStack.EMPTY;
        }
        return iconStack;
    }
}
