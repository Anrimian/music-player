package com.github.anrimian.musicplayer.domain.utils.functions;

import javax.annotation.Nullable;

public class Optional<T> {

    @Nullable
    private final T value;

    public Optional(@Nullable T value) {
        this.value = value;
    }

    public Optional() {
        this.value = null;
    }

    @Nullable
    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Optional<?> optional = (Optional<?>) o;

        return value != null ? value.equals(optional.value) : optional.value == null;
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Optional{" +
                "value=" + value +
                '}';
    }
}
