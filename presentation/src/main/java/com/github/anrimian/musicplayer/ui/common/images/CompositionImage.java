package com.github.anrimian.musicplayer.ui.common.images;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionImage implements ImageMetaData {

    private final Composition composition;

    CompositionImage(Composition composition) {
        this.composition = composition;
    }

    public Composition getComposition() {
        return composition;
    }

    @Override
    public String getKey() {
        return "composition-" + composition.getId();
    }

}
