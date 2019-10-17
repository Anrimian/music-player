package com.github.anrimian.musicplayer.data.storage.providers.music;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class StorageComposition {

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nullable
    private final String album;
    @Nonnull
    private final String filePath;

    private final long duration;
    private final long size;
    private final long id;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    public StorageComposition(@Nullable String artist,
                              @Nullable String title,
                              @Nullable String album,
                              @Nonnull String filePath,
                              long duration,
                              long size,
                              long id,
                              @Nonnull Date dateAdded,
                              @Nonnull Date dateModified) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public StorageComposition copy(String newPath) {
        return new StorageComposition(artist,
                title,
                album,
                newPath,
                duration,
                size,
                id,
                dateAdded,
                dateModified);
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nonnull
    public String getFilePath() {
        return filePath;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getId() {
        return id;
    }

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @Override
    public String toString() {
        return "Composition{" +
                "\n id=" + id +
                "\n filePath='" + filePath + '\'' +
                "\n duration=" + duration +
                "\n size=" + size +
                "\n dateAdded=" + dateAdded +
                "\n dateModified=" + dateModified +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageComposition that = (StorageComposition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
