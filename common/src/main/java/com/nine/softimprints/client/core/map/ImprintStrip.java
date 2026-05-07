package com.nine.softimprints.client.core.map;

import java.util.function.Consumer;

public record ImprintStrip(int x0, int y0, int x1, byte value) {

    public ImprintStrip {
    }

    public int length() {
        return x1 - x0;
    }

    public static void consume(Consumer<ImprintStrip> output, IImprintMap map) {
        int size = map.size();
        for (int y = 0; y < size; y++) {
            int stripStart = 0;
            byte current = map.get(0, y);

            for (int x = 1; x < size; x++) {
                byte value = map.get(x, y);
                if (value != current) {
                    output.accept(new ImprintStrip(stripStart, y, x, current));
                    stripStart = x;
                    current = value;
                }
            }

            output.accept(new ImprintStrip(stripStart, y, size, current));
        }
    }
}
