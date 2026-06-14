package com.nine.softimprints.profile.options.block;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.Objects;

public record SurfaceBlock(Identifier id, @Nullable Block block) {

    public static SurfaceBlock of(Block block) {
        Objects.requireNonNull(block);
        return new SurfaceBlock(BuiltInRegistries.BLOCK.getKey(block), block);
    }

    public static SurfaceBlock of(Identifier id) {
        var opt = BuiltInRegistries.BLOCK.get(id);
        return new SurfaceBlock(id, opt.map(Holder.Reference::value).orElse(null));
    }

    public boolean resolved() {
        return block != null;
    }

}
