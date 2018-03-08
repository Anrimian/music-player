package com.github.anrimian.simplemusicplayer.utils.java;

/**
 * Created on 08.03.2018.
 */

public class ValueFilter {

    public static float filter(float value, float start, float end) {
        if (value <= start) {
            return 0.0f;
        } else if (value >= end) {
            return 1.0f;
        } else {
            return (value - start) / (end - start);
        }
    }
}
