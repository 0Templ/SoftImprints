package com.nine.softimprints.client.ui.component.search;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Optional;

public final class EntitySearchEntry implements SearchableEntry<EntityType<?>> {

    private final EntityType<?> entityType;
    private final String displayName;
    private final String registryKey;
    private final Optional<Item> spawnEggItem;

    private ItemStack iconStack = ItemStack.EMPTY;
    private boolean iconResolved = false;

    public EntitySearchEntry(EntityType<?> entityType) {
        this.entityType = entityType;
        this.displayName = entityType.getDescription().getString();
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
        this.registryKey = key.toString();
        Identifier eggId = Identifier.tryParse(key.getNamespace() + ":" + key.getPath() + "_spawn_egg");
        this.spawnEggItem = eggId != null
                ? BuiltInRegistries.ITEM.getOptional(eggId)
                : Optional.empty();
    }

    @Override
    public EntityType<?> value() {
        return entityType;
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
        Item item = spawnEggItem.orElse(Items.BARRIER);
        try {
            iconStack = item.getDefaultInstance();
            iconResolved = true;
        } catch (Exception ignored) {
            iconStack = ItemStack.EMPTY;
        }
        return iconStack;
    }
}
