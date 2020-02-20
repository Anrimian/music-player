package com.github.anrimian.musicplayer.domain.models.composition.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

@Deprecated
public class MusicFileSource implements FileSource {

    @Nonnull
    private Composition composition;

    public MusicFileSource(@Nonnull Composition composition) {
        this.composition = composition;
    }

    @Override
    public String getPath() {
        return composition.getFilePath();
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
