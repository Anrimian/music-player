package com.github.anrimian.musicplayer.domain.models.utils;

public class ListPosition {

    private final int position;
    private final int offset;

    public ListPosition(int position, int offset) {
        this.position = position;
        this.offset = offset;
    }

    public int getPosition() {
        return position;
    }

    public int getOffset() {
        return offset;
    }

    @Override
    public String toString() {
        return "ListPosition{" +
                "position=" + position +
                ", offset=" + offset +
                '}';
    }
}
