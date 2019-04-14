package com.github.anrimian.musicplayer.domain.utils.model;

public class Item<T> {

    private final T data;
    private final int position;

    public Item(T data, int position) {
        this.data = data;
        this.position = position;
    }

    public T getData() {
        return data;
    }

    public int getPosition() {
        return position;
    }
}
