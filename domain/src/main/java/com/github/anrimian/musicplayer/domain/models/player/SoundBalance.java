package com.github.anrimian.musicplayer.domain.models.player;

public class SoundBalance {

    private final float left;
    private final float right;

    public SoundBalance(float left, float right) {
        this.left = left;
        this.right = right;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }
}
