package com.github.anrimian.musicplayer.data.models.composition.source;

import android.net.Uri;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;

public class UriCompositionSource implements CompositionSource {

    private final Uri uri;

    public UriCompositionSource(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UriCompositionSource source = (UriCompositionSource) o;

        return uri.equals(source.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
