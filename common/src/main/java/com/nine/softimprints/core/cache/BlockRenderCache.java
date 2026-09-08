package com.nine.softimprints.core.cache;

import com.nine.softimprints.model.BlockRenderData;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

public class BlockRenderCache {

    private final ConcurrentHashMap<Long, BlockRenderData> models = new ConcurrentHashMap<>();
    private final LongPredicate hasImprint;

    public BlockRenderCache() {
        this(pos -> true);
    }

    public BlockRenderCache(LongPredicate hasImprint) {
        this.hasImprint = hasImprint;
    }

    public BlockRenderData get(
            Long pos,
            BlockStateModel wrapped,
            BlockState state
    ) {
        BlockRenderData data = models.computeIfAbsent(pos, k -> hasImprint.test(k)
                ? BlockRenderData.compute(BlockPos.of(k), wrapped, state)
                : null);
        return data != null ? data : BlockRenderData.compute(BlockPos.of(pos), wrapped, state);
    }

    public void clearAt(Long pos) {
        models.remove((long) pos);
    }

    public void clearIf(LongPredicate predicate) {
        models.keySet().removeIf(predicate::test);
    }

}
