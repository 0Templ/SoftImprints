package com.nine.softimprints.client.api.plugin;

import com.nine.softimprints.client.profile.ProfileResolverEntry;
import net.minecraft.world.level.block.Block;

import java.util.Collection;

public interface ImprintRegistrar {

    void registerResolver(Block block, ProfileResolverEntry resolver);

    default void registerResolver(Collection<Block> blocks, ProfileResolverEntry resolver) {
        blocks.forEach(block -> registerResolver(block, resolver));
    }

}
