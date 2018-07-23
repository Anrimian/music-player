package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MusicFileSource that = (MusicFileSource) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }

    @Override
    public String toString() {
        return "MusicFileSource{" +
                "composition=" + composition +
                '}';
    }
}
