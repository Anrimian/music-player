package com.github.anrimian.musicplayer.ui.common.images;

public class CompositionImage implements ImageMetaData {

    private final long id;

    CompositionImage(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    @Override
    public String getKey() {
        return "composition-" + id;
    }

}
