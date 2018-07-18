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

    private Date latestCreateDate;
    private Date earliestCreateDate;

    public FolderFileSource(@Nonnull String fullPath,
                            int filesCount,
                            @Nonnull Date newestCreateDate,
                            @Nonnull Date earliestCreateDate) {
        this.fullPath = fullPath;
        this.filesCount = filesCount;
        this.latestCreateDate = newestCreateDate;
        this.earliestCreateDate = earliestCreateDate;
    }

    @Nonnull
    public String getFullPath() {
        return fullPath;
    }

    public int getFilesCount() {
        return filesCount;
    }

    public Date getLatestCreateDate() {
        return latestCreateDate;
    }

    public Date getEarliestCreateDate() {
        return earliestCreateDate;
    }

    @Override
    public String toString() {
        return "FolderFileSource{" +
                "fullPath='" + fullPath + '\'' +
                ", filesCount=" + filesCount +
                '}';
    }
}
