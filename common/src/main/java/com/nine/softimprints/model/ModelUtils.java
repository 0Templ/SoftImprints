package com.nine.softimprints.model;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ModelUtils {


    public static float resolveTopY(
            BlockState state,
            BlockGetter level,
            BlockPos pos
    ) {
        if (state.getBlock() instanceof SnowLayerBlock) {
            return state.getValue(SnowLayerBlock.LAYERS) / 8.0f;
        }
        VoxelShape shape = state.getShape(level, pos);
        if (!shape.isEmpty()) {
            return (float) Mth.clamp(shape.max(Direction.Axis.Y), 0.0, 1.0);
        }
        return 1.0f;
    }


}
