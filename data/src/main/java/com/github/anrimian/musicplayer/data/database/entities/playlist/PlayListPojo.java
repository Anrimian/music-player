package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.annotation.NonNull;

import java.util.Date;

import javax.annotation.Nonnull;

public class PlayListPojo {

    private final long id;

    @Nonnull
    private final String name;

    @Nonnull
    private final Date dateAdded;

    @Nonnull
    private final Date dateModified;

    private final int compositionsCount;

    private final long totalDuration;

    public PlayListPojo(long id,
                        @Nonnull String name,
                        @Nonnull Date dateAdded,
                        @Nonnull Date dateModified,
                        int compositionsCount,
                        long totalDuration) {
        this.id = id;
        this.name = name;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.compositionsCount = compositionsCount;
        this.totalDuration = totalDuration;
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
