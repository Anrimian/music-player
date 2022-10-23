package com.github.anrimian.musicplayer.data.utils.collections;

import java.util.Arrays;

public class StringArrayBuilder {

    private int length;
    private String[] array;

    public StringArrayBuilder(String[] array) {
        this.array = array;
        length = array.length;
    }

    // This does the final shrinking.
    public String[] build() {
        return Arrays.copyOf(array, length);
    }

    public StringArrayBuilder append(String element) {
        if (array.length == length) {
            array = Arrays.copyOf(array, 2 * length);
        }
        array[length++] = element;
        return this;
    }
}