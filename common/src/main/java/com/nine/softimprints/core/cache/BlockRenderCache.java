package com.nine.softimprints.core.cache;

import com.nine.softimprints.model.BlockRenderData;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

public class BlockRenderCache {

    private final ConcurrentHashMap<Long, Entry> models = new ConcurrentHashMap<>();
    private final LongPredicate hasImprint;

    private record Entry(BlockStateModel model, BlockState state, BlockRenderData data) {}

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
        Entry current = models.get(pos);
        if (current != null && current.model() == wrapped && current.state() == state) {
            return current.data();
        }

        Entry entry = models.compute(pos, (k, cached) -> {
            if (cached != null && cached.model() == wrapped && cached.state() == state) {
                return cached;
            }
            // Keep this check inside compute so removed imprints are not cached again.
            if (!hasImprint.test(k)) {
                return null;
            }
            return new Entry(wrapped, state, BlockRenderData.compute(BlockPos.of(k), wrapped, state));
        });
        return entry != null ? entry.data() : BlockRenderData.compute(BlockPos.of(pos), wrapped, state);
    }

    public void clearAt(Long pos) {
        models.remove((long) pos);
    }

    public void clearIf(LongPredicate predicate) {
        models.keySet().removeIf(predicate::test);
    }

}
