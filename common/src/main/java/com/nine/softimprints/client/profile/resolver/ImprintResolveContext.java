package com.nine.softimprints.client.profile.resolver;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public record ImprintResolveContext(
        BlockGetter level,
        BlockPos pos,
        BlockState state
) {

    public Block block() {
        return state.getBlock();
    }
}
