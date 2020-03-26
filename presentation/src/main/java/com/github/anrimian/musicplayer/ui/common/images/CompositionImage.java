package com.github.anrimian.musicplayer.ui.common.images;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionImage implements ImageMetaData {

    private final long id;

    CompositionImage(long id) {
        this.id = id;
    }

    long getId() {
        return id;
    }

    @Override
    public String getKey() {
        return "composition-" + id;
    }

}
