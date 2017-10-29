package com.github.anrimian.simplemusicplayer.ui.library.storage.models;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import javax.annotation.Nullable;

/**
 * Created on 29.10.2017.
 */

public class MusicFileSource {

    @Nullable
    private String path;

    @Nullable
    private Composition composition;

    @Nullable
    public String getPath() {
        return path;
    }

    public void setPath(@Nullable String path) {
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
