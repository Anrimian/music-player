package com.github.anrimian.musicplayer.domain.models.composition.folders;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 31.10.2017.
 */

public class FolderFileSource implements FileSource {

    @Nonnull
    private String fullPath;

    private int filesCount;

    @Nullable
    private Date latestCreateDate;

    @Nullable
    private Date earliestCreateDate;

    public FolderFileSource(@Nonnull String fullPath,
                            int filesCount,
                            @Nullable Date newestCreateDate,
                            @Nullable Date earliestCreateDate) {
        this.fullPath = fullPath;
        this.filesCount = filesCount;
        this.latestCreateDate = newestCreateDate;
        this.earliestCreateDate = earliestCreateDate;
    }

    @Override
    @Nonnull
    public String getPath() {
        return fullPath;
    }

    public int getFilesCount() {
        return filesCount;
    }

    @Nullable
    public Date getLatestCreateDate() {
        return latestCreateDate;
    }

    @Nullable
    public Date getEarliestCreateDate() {
        return earliestCreateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FolderFileSource that = (FolderFileSource) o;

        return fullPath.equals(that.fullPath);
    }

    @Override
    public int hashCode() {
        return fullPath.hashCode();
    }

    @Override
    public String toString() {
        return "FolderFileSource{" +
                "fullPath='" + fullPath + '\'' +
                ", filesCount=" + filesCount +
                '}';
    }
}
