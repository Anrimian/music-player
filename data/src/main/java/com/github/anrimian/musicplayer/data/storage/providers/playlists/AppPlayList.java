package com.github.anrimian.musicplayer.data.storage.providers.playlists;

import androidx.annotation.NonNull;

import java.util.Date;

import javax.annotation.Nonnull;

public class AppPlayList {

    private final long id;

    private final long storageId;

    @Nonnull
    private final String name;

    @Nonnull
    private final Date dateAdded;

    @Nonnull
    private final Date dateModified;

    private final int compositionsCount;

    public AppPlayList(long id,
                       long storageId,
                       @Nonnull String name,
                       @Nonnull Date dateAdded,
                       @Nonnull Date dateModified,
                       int compositionsCount) {
        this.id = id;
        this.storageId = storageId;
        this.name = name;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.compositionsCount = compositionsCount;
    }

    public long getId() {
        return id;
    }

    public long getStorageId() {
        return storageId;
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

    public int getCompositionsCount() {
        return compositionsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppPlayList that = (AppPlayList) o;

        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @NonNull
    @Override
    public String toString() {
        return "PlayList{" +
                "id=" + storageId +
                ", name='" + name + '\'' +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                '}';
    }
}
