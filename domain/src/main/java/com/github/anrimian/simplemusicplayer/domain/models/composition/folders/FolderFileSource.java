package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

public class FolderFileSource implements FileSource {

    @Nonnull
    private String path;

    private int filesCount;

    public FolderFileSource(@Nonnull String path, int filesCount) {
        this.path = path;
        this.filesCount = filesCount;
    }

    @Nonnull
    public String getPath() {
        return path;
    }

    public int getFilesCount() {
        return filesCount;
    }

    @Override
    public String toString() {
        return "FolderFileSource{" +
                "path='" + path + '\'' +
                ", filesCount=" + filesCount +
                '}';
    }
}
