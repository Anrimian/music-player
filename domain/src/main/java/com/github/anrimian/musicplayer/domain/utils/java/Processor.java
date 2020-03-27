package com.github.anrimian.musicplayer.domain.utils.java;

public interface Processor<T> {
    T call(T value);
}
