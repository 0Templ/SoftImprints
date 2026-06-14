package com.nine.softimprints.core.cache;

import com.nine.softimprints.model.BlockRenderData;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

public class BlockRenderCache {

    private final ConcurrentHashMap<Long, BlockRenderData> models = new ConcurrentHashMap<>();

    public BlockRenderData get(
            Long pos,
            BlockStateModel wrapped,
            BlockState state
    ) {
        return models.computeIfAbsent(pos, k -> BlockRenderData.compute(BlockPos.of(pos), wrapped, state));
    }

    public void clearAt(Long pos) {
        models.remove((long) pos);
    }

    public void clearIf(LongPredicate predicate) {
        models.keySet().removeIf(predicate::test);
    }

}
