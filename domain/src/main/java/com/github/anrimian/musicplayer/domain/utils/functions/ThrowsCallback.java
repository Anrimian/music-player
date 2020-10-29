package com.github.anrimian.musicplayer.domain.utils.functions;

public interface ThrowsCallback<T> {
    void call(T value) throws Exception;
}
