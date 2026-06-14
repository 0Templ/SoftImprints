package com.nine.softimprints.core.stamp;

public class StampSeedHelper {


    public static int mixSeed(int... values) {
        int h = 1;
        for (int v : values) {
            h = 31 * h + v;
            h ^= (h >>> 16);
        }
        return h;
    }

    public static double createNoise(
            int x,
            int y,
            int seed
    ) {
        int h = seed;
        h = 31 * h + x;
        h = 31 * h + y;
        h ^= (h >>> 16);
        h *= 0x7feb352d;
        h ^= (h >>> 15);
        h *= 0x846ca68b;
        h ^= (h >>> 16);

        return (h & 0x7fffffff) / (double) Integer.MAX_VALUE;
    }

}
