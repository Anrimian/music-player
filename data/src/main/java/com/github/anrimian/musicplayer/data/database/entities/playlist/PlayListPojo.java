package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.annotation.NonNull;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayListPojo {

    private final long id;

    @Nullable
    private final Long storageId;

    @Nonnull
    private final String name;

    @Nonnull
    private final Date dateAdded;

    @Nonnull
    private final Date dateModified;

    private final int compositionsCount;

    private final long totalDuration;

    public PlayListPojo(long id,
                        @Nullable Long storageId,
                        @Nonnull String name,
                        @Nonnull Date dateAdded,
                        @Nonnull Date dateModified,
                        int compositionsCount,
                        long totalDuration) {
        this.id = id;
        this.storageId = storageId;
        this.name = name;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.compositionsCount = compositionsCount;
        this.totalDuration = totalDuration;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public int getCompositionsCount() {
        return compositionsCount;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayList{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                '}';
    }
}
