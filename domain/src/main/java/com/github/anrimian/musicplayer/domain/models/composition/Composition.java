package com.github.anrimian.musicplayer.domain.models.composition;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class Composition {

    @Nullable
    private String artist;
    private String title;
    private String album;
    private String filePath;

    private long duration;
    private long size;
    private long id;

    private Date dateAdded;
    private Date dateModified;

    private boolean isCorrupted;

    public Composition(@Nullable String artist,
                       String title,
                       String album,
                       String filePath,
                       long duration,
                       long size,
                       long id,
                       Date dateAdded,
                       Date dateModified,
                       boolean isCorrupted) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.id = id;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.isCorrupted = isCorrupted;
    }

    @Deprecated
    public Composition() {
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
                isCorrupted);
    }

    public boolean isCorrupted() {
        return isCorrupted;
    }

    public void setCorrupted(boolean corrupted) {
        isCorrupted = corrupted;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    public void setArtist(@Nullable String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
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
