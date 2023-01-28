package com.github.anrimian.musicplayer.data.storage.providers.music;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.data.storage.providers.albums.StorageAlbum;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class StorageFullComposition {

    @Nullable
    private final String artist;
    @Nullable
    private final String title;
    @Nonnull
    private final String fileName;

    private final long duration;
    private final long size;
    private final long storageId;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;

    private final StorageAlbum storageAlbum;

    @Nonnull
    private String relativePath;

    public StorageFullComposition(@Nullable String artist,
                                  @Nullable String title,
                                  @Nonnull String fileName,
                                  @Nonnull String relativePath,
                                  long duration,
                                  long size,
                                  long storageId,
                                  @Nonnull Date dateAdded,
                                  @Nonnull Date dateModified,
                                  StorageAlbum storageAlbum) {
        this.artist = artist;
        this.title = title;
        this.fileName = fileName;
        this.relativePath = relativePath;
        this.duration = duration;
        this.size = size;
        this.storageId = storageId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.storageAlbum = storageAlbum;
    }

    public void setRelativePath(@Nonnull String relativePath) {
        this.relativePath = relativePath;
    }

    @Nonnull
    public String getRelativePath() {
        return relativePath;
    }

    public StorageAlbum getStorageAlbum() {
        return storageAlbum;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getStorageId() {
        return storageId;
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
        return "\n StorageFullComposition{" +
                "\n artist='" + artist + '\'' +
                ",\n title='" + title + '\'' +
                ",\n relativePath='" + relativePath + '\'' +
                ",\n id=" + storageId +
                "\n }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StorageFullComposition that = (StorageFullComposition) o;

        return storageId == that.storageId;

    }

    @Override
    public int hashCode() {
        return (int) (storageId ^ (storageId >>> 32));
    }
}
