package com.nine.softimprints.client.core.cache;

import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.core.Constants;
import com.nine.softimprints.client.core.map.IImprintMap;
import com.nine.softimprints.client.core.map.ImprintMap;
import com.nine.softimprints.client.core.placement.BlockMask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

public class ImprintCache {

    private final ConcurrentHashMap<Long, ImprintBlockMap> maps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastTouchedTick = new ConcurrentHashMap<>();

    private final Set<Long> dirtySections = new HashSet<>();

    public ImprintCache() {

    }

    public IImprintMap getImprintMap(Long pos){
        var map = maps.getOrDefault(pos, null);
        if (map == null) return null;

        return new ImprintMap(map.mapSize(), Arrays.copyOf(map.values(), map.values().length));
    }

    public void apply(List<BlockMask> masks, long gameTime){
        boolean changed = false;
        for (var mask : masks){
//            var current = maps.get(mask.blockPos());
//            if (current == null || current.mapSize() != mask.mapSize()) {
//                current = new ImprintBlockMap(mask.mapSize(), Arrays.copyOf(mask.map(), mask.map().length));
//            }
            ImprintBlockMap map = maps.get(mask.blockPos());
            if (map == null || map.mapSize() != mask.mapSize()) {
                map = new ImprintBlockMap(
                        mask.mapSize(),
                        new byte[mask.mapSize() * mask.mapSize()]);
                maps.put(mask.blockPos(), map);
            }
//            ImprintBlockMap map = maps.computeIfAbsent(mask.blockPos(),
//                    pos -> {
//                        return new ImprintBlockMap(
//                                mask.mapSize(),
//                                new byte[mask.mapSize() * mask.mapSize()]);
//                    });
            if (rasterize(map, mask)){
                dirtySections.add(sectionOf(mask.blockPos()));
                lastTouchedTick.put(mask.blockPos(), gameTime);
                changed = true;
            }
        }
        if (changed) evictOverflow();
    }


/*    public void apply(List<BlockMask> masks, long gameTime){
        boolean changed = false;
        for (var mask : masks){
            var current = maps.get(mask.blockPos());
            if (current == null || current.mapSize() != mask.mapSize()) {
                current = new ImprintBlockMap(mask.mapSize(), Arrays.copyOf(mask.map(), mask.map().length));
            }
            if (rasterize(current, mask)){
                dirtySections.add(sectionOf(mask.blockPos()));
                lastTouchedTick.put(mask.blockPos(), gameTime);
                changed = true;
            }
        }
        if (changed) evictOverflow();
    }*/


    private void evictOverflow() {
        int max = SIConfig.Performance.MAX_CACHED_IMPRINT_BLOCKS.get();
        if (max == SIConfig.Performance.MAX_CACHED_IMPRINT_BLOCKS.max().intValue()
                || max <= 0|| maps.size() <= max) return;

        int toRemove = maps.size() - max;

        lastTouchedTick.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .forEach(m -> removeMap(m, true));
    }

    public List<Long> drainSections(int amount) {
        if (amount <= 0) {
            return List.of();
        }
        List<Long> ret = new ArrayList<>();
        Iterator<Long> iterator = dirtySections.iterator();

        while (iterator.hasNext() && ret.size() < amount) {
            ret.add(iterator.next());
            iterator.remove();
        }
        return ret;
    }

    public List<Long> drainAllSections(){
        var ret = new ArrayList<>(dirtySections);
        dirtySections.clear();
        return ret;
    }

    public void clearAt(Long pos){
        removeMap(pos, true);
    }

    public void clearIf(LongPredicate predicate) {
        for (long pos : List.copyOf(maps.keySet())) {
            if (predicate.test(pos)) {
                removeMap(pos, true);
            }
        }
    }

    private void removeMap(long blockPos, boolean markDirty) {
        maps.remove(blockPos);
        lastTouchedTick.remove(blockPos);
        if (markDirty) {
            dirtySections.add(sectionOf(blockPos));
        }
    }

    private boolean rasterize(ImprintBlockMap map, BlockMask mask) {
        var dst = map.values();
        byte[] src = mask.map();
        boolean changed = false;

        for (int i = 0; i < (map.mapSize() * map.mapSize()); i++) {
            byte s = src[i];
            if (s == 0) continue;

            byte d = dst[i];
            if (d == 0 || s < d) {
                dst[i] = s;
                changed = true;
            }
        }
        return changed;
    }

    private static long sectionOf(long pos){
        return SectionPos.asLong(
                SectionPos.blockToSectionCoord(BlockPos.getX(pos)),
                SectionPos.blockToSectionCoord(BlockPos.getY(pos)),
                SectionPos.blockToSectionCoord(BlockPos.getZ(pos))
        );
    }



}
