package com.nine.softimprints.core.decay;

import com.nine.softimprints.config.SIConfig;
import com.nine.softimprints.core.cache.ImprintBlockMap;
import com.nine.softimprints.core.cache.ImprintCache;
import com.nine.softimprints.core.stamp.StampSeedHelper;
import com.nine.softimprints.profile.ImprintProfile;
import com.nine.softimprints.profile.ImprintProfiles;
import com.nine.softimprints.profile.options.decay.ProfileDecaySettings;
import com.nine.softimprints.profile.options.layer.ImprintLayer;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;

public final class ImprintDecayPass {

    private static final int DECAY_SALT = 0x9E3779B9;
    private static final int PHASE_SALT = 0x27220A95;
    private static final int WARP_SALT = 0x51E7B2C3;
    private static final int SETTLE_SALT = 0x3C6EF372;
    private static final int CELL_PHASE_SALT = 0x68E31DA4;

    private static final double STATIC_NOISE_WEIGHT = 0.65D;

    private static final double SETTLE_FACTOR = 0.15D;

    private static final int NOISE_PERIOD = 5;

    private static final int MAX_BLOCKS_PER_TICK = 256;

    private static final int TICKS_PER_SECOND = 20;

    private static final byte[] SEAM_BLOCKED = new byte[0];

    private static final int WEST = 0;
    private static final int EAST = 1;
    private static final int NORTH = 2;
    private static final int SOUTH = 3;

    public void tick(
            ClientLevel level,
            ImprintCache cache,
            long tickCounter
    ) {
        if (!SIConfig.Decay.ENABLE_IMPRINT_DECAY.get()) return;

        int rate = SIConfig.Decay.IMPRINT_DECAY_TICK_RATE.get();
        long now = level.getGameTime();

        int processed = 0;
        for (long pos : cache.positionsSnapshot()) {
            if ((tickCounter + phaseOf(pos, rate)) % rate != 0L) continue;

            decayMap(level, cache, pos, now, tickCounter, rate);
            if (++processed >= MAX_BLOCKS_PER_TICK) break;
        }
    }

    private static int phaseOf(
            long pos,
            int rate
    ) {
        return (StampSeedHelper.mixSeed((int) pos, (int) (pos >>> 32), PHASE_SALT) & 0x7fffffff) % rate;
    }

    private void decayMap(
            ClientLevel level,
            ImprintCache cache,
            long pos,
            long now,
            long tickCounter,
            int rate
    ) {
        BlockPos blockPos = BlockPos.of(pos);
        BlockState state = level.getBlockState(blockPos);
        ImprintProfile profile = ImprintProfiles.getProfile(level, blockPos, state);
        if (profile == null) return;

        ProfileDecaySettings decay = profile.decay();
        if (!decay.enabled()) return;

        Long touched = cache.lastTouched(pos);
        long age = touched == null ? Long.MAX_VALUE : now - touched;
        long ripe = age - graceTicks(decay);
        if (ripe <= 0L) return;

        byte[] ladder = enabledLayerValues(profile);
        if (ladder.length == 0) return;

        double ageFactor = Math.min(1.0D, ripe / (double) rampTicks(decay));

        int subdiv = Math.max(1, SIConfig.Decay.IMPRINT_DECAY_CELL_PHASES.get());
        long passIndex = Long.divideUnsigned(tickCounter, rate);
        int cycle = (int) Long.divideUnsigned(passIndex, subdiv);
        int step = (int) Math.floorMod(passIndex, subdiv);
        int activeSlot = shuffledSlot(step, cycle, subdiv);
        double chance = decay.chanceOrDefault() * ageFactor * subdiv;

        int passSeed = StampSeedHelper.mixSeed((int) tickCounter, DECAY_SALT);
        double warpMin = SIConfig.Decay.IMPRINT_DECAY_WARP_RATE_MIN.get();
        double warpSpan = SIConfig.Decay.IMPRINT_DECAY_WARP_RATE_SPAN.get();

        cache.rewriteInPlace(pos, map -> stepCells(map, seamsOf(cache, pos, map.mapSize()), ladder, chance,
                decay.depthBiasOrDefault(), blockPos, passSeed, subdiv, activeSlot, warpMin, warpSpan));
    }

    private static long graceTicks(ProfileDecaySettings decay) {
        return decay.graceSecondsOrDefault() * (long) TICKS_PER_SECOND;
    }

    private static long rampTicks(ProfileDecaySettings decay) {
        return Math.max(1L, decay.rampSecondsOrDefault() * (long) TICKS_PER_SECOND);
    }

    private static boolean stepCells(
            ImprintBlockMap map,
            byte[][] seams,
            byte[] ladder,
            double baseChance,
            double depthBias,
            BlockPos blockPos,
            int passSeed,
            int subdiv,
            int activeSlot,
            double warpMin,
            double warpSpan
    ) {
        int size = map.mapSize();
        byte[] values = map.values();
        byte[] src = values.clone();
        boolean changed = false;

        int baseX = blockPos.getX() * size;
        int baseZ = blockPos.getZ() * size;

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                int i = y * size + x;
                byte value = src[i];
                if (value == 0) continue;

                if (subdiv > 1 && cellPhase(baseX + x, baseZ + y, subdiv) != activeSlot) continue;

                int front = shallowerNeighbours(src, seams, size, x, y, value);
                if (front > 0) {
                    double warpRate = warpMin + warpSpan * warpNoise(baseX + x, baseZ + y);
                    double chance = baseChance * Math.sqrt(front) * warpRate
                            * depthResistance(ladder, value, depthBias);
                    if (StampSeedHelper.createNoise(baseX + x, baseZ + y, passSeed) >= chance) continue;
                } else if (hasDeeperNeighbour(src, seams, size, x, y, value)) {
                    continue;
                } else {
                    double chance = baseChance * SETTLE_FACTOR * depthResistance(ladder, value, depthBias);
                    double resistance = STATIC_NOISE_WEIGHT * StampSeedHelper.createNoise(baseX + x, baseZ + y, SETTLE_SALT)
                            + (1.0D - STATIC_NOISE_WEIGHT) * StampSeedHelper.createNoise(baseX + x, baseZ + y, passSeed);
                    if (resistance >= chance) continue;
                }

                values[i] = shallower(ladder, value);
                changed = true;
            }
        }
        return changed;
    }

    private static int cellPhase(
            int worldX,
            int worldZ,
            int subdiv
    ) {
        double n = StampSeedHelper.createNoise(worldX, worldZ, CELL_PHASE_SALT);
        int slot = (int) (n * subdiv);
        return slot >= subdiv ? subdiv - 1 : slot;
    }

    private static int shuffledSlot(
            int step,
            int cycle,
            int subdiv
    ) {
        if (subdiv <= 1) return 0;
        int key = StampSeedHelper.mixSeed(cycle, CELL_PHASE_SALT);
        int offset = Math.floorMod(key, subdiv);
        int rot = Math.floorMod(step + offset, subdiv);
        return (key >>> 16 & 1) == 0 ? rot : subdiv - 1 - rot;
    }

    private static double depthResistance(
            byte[] ladder,
            byte value,
            double depthBias
    ) {
        if (ladder.length <= 1) return 1.0D;
        int index = Arrays.binarySearch(ladder, value);
        if (index < 0) return 1.0D;
        double depthFraction = (ladder.length - 1 - index) / (double) (ladder.length - 1);
        return 1.0D - depthBias * depthFraction;
    }

    private static double warpNoise(
            int x,
            int y
    ) {
        int cellX = Math.floorDiv(x, NOISE_PERIOD);
        int cellY = Math.floorDiv(y, NOISE_PERIOD);
        double tx = fade(Math.floorMod(x, NOISE_PERIOD) / (double) NOISE_PERIOD);
        double ty = fade(Math.floorMod(y, NOISE_PERIOD) / (double) NOISE_PERIOD);
        double n00 = StampSeedHelper.createNoise(cellX, cellY, WARP_SALT);
        double n10 = StampSeedHelper.createNoise(cellX + 1, cellY, WARP_SALT);
        double n01 = StampSeedHelper.createNoise(cellX, cellY + 1, WARP_SALT);
        double n11 = StampSeedHelper.createNoise(cellX + 1, cellY + 1, WARP_SALT);
        double top = n00 + tx * (n10 - n00);
        double bottom = n01 + tx * (n11 - n01);
        return top + ty * (bottom - top);
    }

    private static double fade(double t) {
        return t * t * (3.0D - 2.0D * t);
    }

    private static int shallowerNeighbours(
            byte[] src,
            byte[][] seams,
            int size,
            int x,
            int y,
            byte value
    ) {
        int count = 0;
        if (isShallower(neighbourValue(src, seams, size, x - 1, y), value)) count++;
        if (isShallower(neighbourValue(src, seams, size, x + 1, y), value)) count++;
        if (isShallower(neighbourValue(src, seams, size, x, y - 1), value)) count++;
        if (isShallower(neighbourValue(src, seams, size, x, y + 1), value)) count++;
        return count;
    }

    private static boolean isShallower(
            byte neighbour,
            byte value
    ) {
        return neighbour == 0 || neighbour > value;
    }

    private static boolean hasDeeperNeighbour(
            byte[] src,
            byte[][] seams,
            int size,
            int x,
            int y,
            byte value
    ) {
        return isDeeper(neighbourValue(src, seams, size, x - 1, y), value)
                || isDeeper(neighbourValue(src, seams, size, x + 1, y), value)
                || isDeeper(neighbourValue(src, seams, size, x, y - 1), value)
                || isDeeper(neighbourValue(src, seams, size, x, y + 1), value);
    }

    private static boolean isDeeper(
            byte neighbour,
            byte value
    ) {
        return neighbour != 0 && neighbour < value;
    }

    private static byte neighbourValue(
            byte[] src,
            byte[][] seams,
            int size,
            int x,
            int y
    ) {
        if (x < 0) return seamValue(seams[WEST], size, size - 1, y);
        if (x >= size) return seamValue(seams[EAST], size, 0, y);
        if (y < 0) return seamValue(seams[NORTH], size, x, size - 1);
        if (y >= size) return seamValue(seams[SOUTH], size, x, 0);
        return src[y * size + x];
    }

    private static byte seamValue(
            byte[] seam,
            int size,
            int x,
            int y
    ) {
        if (seam == null) return 0;
        if (seam == SEAM_BLOCKED) return -1;
        return seam[y * size + x];
    }

    private static byte[][] seamsOf(
            ImprintCache cache,
            long pos,
            int size
    ) {
        BlockPos blockPos = BlockPos.of(pos);
        byte[][] seams = new byte[4][];
        seams[WEST] = seamOf(cache, blockPos.west(), size);
        seams[EAST] = seamOf(cache, blockPos.east(), size);
        seams[NORTH] = seamOf(cache, blockPos.north(), size);
        seams[SOUTH] = seamOf(cache, blockPos.south(), size);
        return seams;
    }

    private static byte[] seamOf(
            ImprintCache cache,
            BlockPos pos,
            int size
    ) {
        var map = cache.liveMap(pos.asLong());
        if (map == null) return null;
        if (map.mapSize() != size) return SEAM_BLOCKED;
        return map.values();
    }

    private static byte shallower(
            byte[] ladder,
            byte value
    ) {
        for (byte step : ladder) {
            if (step > value) return step;
        }
        return 0;
    }

    private static byte[] enabledLayerValues(ImprintProfile profile) {
        var layers = profile.getLayers();
        byte[] values = new byte[layers.size()];
        int count = 0;
        for (ImprintLayer layer : layers) {
            if (layer.enable()) {
                values[count++] = layer.value();
            }
        }
        return count == values.length ? values : Arrays.copyOf(values, count);
    }

}
