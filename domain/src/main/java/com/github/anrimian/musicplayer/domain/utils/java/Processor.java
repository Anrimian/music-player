package com.github.anrimian.musicplayer.domain.utils.java;

public interface Processor<T, R> {
    R run(T o);
}
