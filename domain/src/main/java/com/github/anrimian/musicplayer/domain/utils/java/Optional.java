package com.github.anrimian.musicplayer.domain.utils.java;

public class Optional<T> {

    private final T value;

    public Optional(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }
}
