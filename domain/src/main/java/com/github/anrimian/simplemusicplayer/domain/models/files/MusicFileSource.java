package com.github.anrimian.simplemusicplayer.domain.models.files;

import com.github.anrimian.simplemusicplayer.domain.models.Composition;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSource implements FileSource {

    @Nonnull
    private Composition composition;

    public MusicFileSource(@Nonnull Composition composition) {
        this.composition = composition;
    }

    @Nonnull
    public Composition getComposition() {
        return composition;
    }
}
