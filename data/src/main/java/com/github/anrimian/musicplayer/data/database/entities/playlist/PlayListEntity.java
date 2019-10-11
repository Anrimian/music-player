package com.github.anrimian.musicplayer.data.database.entities.playlist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import javax.annotation.Nonnull;

@Entity(tableName = "play_lists")
public class PlayListEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long storageId;

    @Nonnull
    private String name;

    @Nonnull
    private Date dateAdded;

    @Nonnull
    private Date dateModified;

    public PlayListEntity(long storageId,
                          @Nonnull String name,
                          @Nonnull Date dateAdded,
                          @Nonnull Date dateModified) {
        this.storageId = storageId;
        this.name = name;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
    }

    public void setId(long id) {
        this.id = id;
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
}
