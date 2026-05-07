package com.nine.softimprints.client.core.map;

public interface IImprintMap {

    int size();

    byte[] data();

    byte get(int x, int y);

    default boolean isEmpty() {
        int size = size();
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (get(x, y) != 0) {
                    return false;
                }
            }
        }
        return true;
    }

}
