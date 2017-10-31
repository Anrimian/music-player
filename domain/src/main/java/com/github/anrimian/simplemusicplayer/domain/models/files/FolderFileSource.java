package com.github.anrimian.simplemusicplayer.domain.models.files;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 31.10.2017.
 */

public class FolderFileSource implements FileSource {

    @Nonnull
    private String path;

    public FolderFileSource(@Nonnull String path) {
        this.path = path;
    }

    @Nonnull
    public String getPath() {
        return path;
    }
}
