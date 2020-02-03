package com.github.anrimian.musicplayer.domain.models.composition.folders;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionFileSource2 implements FileSource2 {

    private final Composition composition;

    public CompositionFileSource2(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompositionFileSource2 that = (CompositionFileSource2) o;

        return composition.equals(that.composition);
    }

    @Override
    public int hashCode() {
        return composition.hashCode();
    }
}
