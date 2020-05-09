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
}
