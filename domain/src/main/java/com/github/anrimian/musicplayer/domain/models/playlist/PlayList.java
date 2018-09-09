package com.github.anrimian.musicplayer.domain.models.playlist;

import java.util.Date;

import javax.annotation.Nonnull;

public class PlayList {

    private long id;

    @Nonnull
    private String name;

    @Nonnull
    private Date dateAdded;

    @Nonnull
    private Date dateModified;

    private int compositionsCount;

    private long totalDuration;

    public PlayList(long id,
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayList playList = (PlayList) o;

        return id == playList.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

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
