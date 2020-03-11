package com.github.anrimian.musicplayer.ui.common.images;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;

public class CompositionImage implements ImageMetaData {

    private final long id;
    private final String filePath;

    CompositionImage(long id, String filePath) {
        this.id = id;
        this.filePath = filePath;
    }

    long getId() {
        return id;
    }

    String getFilePath() {
        return filePath;
    }

    @Override
    public String getKey() {
        return "composition-" + id;
    }

}
