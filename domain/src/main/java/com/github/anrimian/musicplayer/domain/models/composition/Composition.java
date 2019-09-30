package com.github.anrimian.musicplayer.domain.models.composition;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class Composition {

    @Nullable
    private String artist;
    @Nullable
    private String title;
    @Nullable
    private String album;
    @Nonnull
    private String filePath;

    private long duration;
    private long size;
    private long id;

    @Nonnull
    private Date dateAdded;
    @Nonnull
    private Date dateModified;

    private CorruptionType corruptionType;

    public Composition(@Nullable String artist,
                       @Nullable String title,
                       @Nullable String album,
                       @Nonnull String filePath,
                       long duration,
                       long size,
                       long id,
                       @Nonnull Date dateAdded,
                       @Nonnull Date dateModified,
                       CorruptionType corruptionType) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;
    }

    public Composition copy(String newPath) {
        return new Composition(artist,
                title,
                album,
                newPath,
                duration,
                size,
                id,
                dateAdded,
                dateModified,
                corruptionType);
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

    public CorruptionType getCorruptionType() {
        return corruptionType;
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

        Composition that = (Composition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
