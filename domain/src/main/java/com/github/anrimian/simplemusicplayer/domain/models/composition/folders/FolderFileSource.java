package com.github.anrimian.simplemusicplayer.domain.models.composition.folders;

import java.util.Date;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

public class FolderFileSource implements FileSource {

    @Nonnull
    private String fullPath;

    private int filesCount;

    private Date newestCreateDate;
    private Date latestCreateDate;

    public FolderFileSource(@Nonnull String fullPath,
                            int filesCount,
                            @Nonnull Date newestCreateDate,
                            @Nonnull Date latestCreateDate) {
        this.fullPath = fullPath;
        this.filesCount = filesCount;
        this.newestCreateDate = newestCreateDate;
        this.latestCreateDate = latestCreateDate;
    }

    @Nonnull
    public String getFullPath() {
        return fullPath;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public Date getNewestCreateDate() {
        return newestCreateDate;
    }

    public Date getLatestCreateDate() {
        return latestCreateDate;
    }

    @Override
    public String toString() {
        return "FolderFileSource{" +
                "fullPath='" + fullPath + '\'' +
                ", filesCount=" + filesCount +
                '}';
    }
}
