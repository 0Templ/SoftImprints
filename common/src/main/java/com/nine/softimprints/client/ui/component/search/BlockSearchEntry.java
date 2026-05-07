package com.nine.softimprints.client.ui.component.search;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public final class BlockSearchEntry implements SearchableEntry<Block> {

    private final Block block;
    private final String displayName;
    private final String registryKey;
    private final Item item;

    private ItemStack iconStack = ItemStack.EMPTY;
    private boolean iconResolved = false;

    public BlockSearchEntry(Block block) {
        this.block = block;
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
        IconNameRowRenderer.render(
                graphics, x, y, width, height, mouseX, mouseY,
                resolveIcon(), displayName, hovered, selected
        );
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
