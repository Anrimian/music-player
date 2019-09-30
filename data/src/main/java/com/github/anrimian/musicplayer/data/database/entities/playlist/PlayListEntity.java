package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import javax.annotation.Nonnull;

@Entity
public class PlayListEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nonnull
    private String name;

    @Nonnull
    private Date dateAdded;

    @Nonnull
    private Date dateModified;

    private int compositionsCount;

    private long totalDuration;

    public PlayListEntity(long id,
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

    public int getCompositionsCount() {
        return compositionsCount;
    }

    public long getTotalDuration() {
        return totalDuration;
    }
}
