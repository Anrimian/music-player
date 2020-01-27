package com.github.anrimian.musicplayer.domain.utils.java;

public interface Mapper<T, R> {
    R map(T o);
}
