package com.github.anrimian.musicplayer.ui.common.images.models;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;

public class UriCompositionImage {

    private final UriCompositionSource source;

    public UriCompositionImage(UriCompositionSource source) {
        this.source = source;
    }

    public UriCompositionSource getSource() {
        return source;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriCompositionImage that = (UriCompositionImage) o;

        return source.equals(that.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }
}
