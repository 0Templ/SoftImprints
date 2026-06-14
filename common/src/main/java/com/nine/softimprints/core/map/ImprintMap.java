package com.nine.softimprints.core.map;

public final class ImprintMap implements IImprintMap {

    private final int size;
    private final byte[] data;

    public ImprintMap(int size) {
        this(size, new byte[size * size]);
    }

    public ImprintMap(
            int size,
            byte[] bytes
    ) {
        this.size = size;
        this.data = bytes;
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public byte[] data() {
        return this.data;
    }

    @Override
    public byte get(
            int x,
            int y
    ) {
        return this.data[indexOf(x, y)];
    }

    private int indexOf(
            int x,
            int y
    ) {
        return y * this.size + x;
    }

}
