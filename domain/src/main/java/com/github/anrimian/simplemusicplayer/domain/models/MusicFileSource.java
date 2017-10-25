package com.github.anrimian.simplemusicplayer.domain.models;

import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class MusicFileSource {

    private String path;

    @Nullable
    private Composition composition;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Nullable
    public Composition getComposition() {
        return composition;
    }

    public void setComposition(@Nullable Composition composition) {
        this.composition = composition;
    }
}
