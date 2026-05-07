package com.nine.softimprints.client.core.cache;

import com.nine.softimprints.client.config.SIConfig;
import com.nine.softimprints.client.core.map.IImprintMap;
import com.nine.softimprints.client.core.map.ImprintMap;
import com.nine.softimprints.client.core.placement.BlockMask;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongPredicate;

public class ImprintCache {

    private final ConcurrentHashMap<Long, byte[]> maps = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> lastTouchedTick = new ConcurrentHashMap<>();

    private final Set<Long> dirtySections = new HashSet<>();

    public ImprintCache() {

    }

    public ConcurrentHashMap<Long, byte[]> getMaps() {
        return maps;
    }

    public static ImprintCache createDefault() {
        return new ImprintCache();
    }

    public IImprintMap getImprintMap(Long pos){
        var bytes = maps.getOrDefault(pos, null);
        if (bytes == null) return null;

        return new ImprintMap(16, Arrays.copyOf(bytes, bytes.length));
    }

    public void apply(List<BlockMask> masks, long gameTime){
        boolean changed = false;
        for (var mask : masks){
            byte[] map = maps.computeIfAbsent(mask.blockPos(), pos -> new byte[16*16]);
            if (rasterize(map, mask)){
                dirtySections.add(sectionOf(mask.blockPos()));
                lastTouchedTick.put(mask.blockPos(), gameTime);
                changed = true;
            }
        }
        if (changed) evictOverflow();
    }

    private void evictOverflow() {
        int max = SIConfig.Performance.MAX_CACHED_IMPRINT_BLOCKS.get();
        if (max == SIConfig.Performance.MAX_CACHED_IMPRINT_BLOCKS.max().intValue()
                || max <= 0|| maps.size() <= max) return;

        int toRemove = maps.size() - max;

        lastTouchedTick.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(toRemove)
                .map(Map.Entry::getKey)
                .forEach(this::removeMapAndMarkDirty);
    }

    private void removeMapAndMarkDirty(long blockPos) {
        maps.remove(blockPos);
        lastTouchedTick.remove(blockPos);
        dirtySections.add(sectionOf(blockPos));
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
        maps.remove(pos);
    }

    public void clearIf(LongPredicate predicate) {
        maps.keySet().removeIf(predicate::test);
    }

    private boolean rasterize(byte[] dst, BlockMask mask) {
        byte[] src = mask.map();
        boolean changed = false;

        for (int i = 0; i < 256; i++) {
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
