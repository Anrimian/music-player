package com.github.anrimian.musicplayer.domain.models.composition;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class Composition {

    @Nullable
    private final String artist;
    @Nonnull
    private final String title;
    @Nullable
    private final String album;

    private final long duration;
    private final long size;
    private final long id;

    @Nullable
    private final Long storageId;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;
    @Nonnull
    private final Date coverModifyTime;

    @Nullable
    private final CorruptionType corruptionType;

    private final boolean isFileExists;

    private final InitialSource initialSource;

    @SuppressWarnings("NullableProblems")//annotations break room annotations processing
    public Composition(String artist,
                       String title,
                       String album,
                       long duration,
                       long size,
                       long id,
                       Long storageId,
                       Date dateAdded,
                       Date dateModified,
                       Date coverModifyTime,
                       CorruptionType corruptionType,
                       boolean isFileExists,
                       InitialSource initialSource) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.storageId = storageId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.coverModifyTime = coverModifyTime;
        this.corruptionType = corruptionType;
        this.isFileExists = isFileExists;
        this.initialSource = initialSource;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nonnull
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getAlbum() {
        return album;
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

    @Nonnull
    public Date getCoverModifyTime() {
        return coverModifyTime;
    }

    @Nullable
    public CorruptionType getCorruptionType() {
        return corruptionType;
    }

    public boolean isFileExists() {
        return isFileExists;
    }

    public InitialSource getInitialSource() {
        return initialSource;
    }

    @Override
    public String toString() {
        return "Composition{" +
                "title='" + title + '\'' +
                ", id=" + id +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Composition)) return false;//compare child classes too

        Composition that = (Composition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
