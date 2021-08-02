package com.github.anrimian.musicplayer.domain.utils;

public class Objects {

    public static boolean equals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    public static <T> T requireNonNull(T obj) {
        if (obj == null)
            throw new NullPointerException("requireNonNull is null");
        return obj;
    }
}
