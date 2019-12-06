package com.github.anrimian.musicplayer.data.database.entities.artist;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "artists")
public class ArtistEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long storageId;

    @Nonnull
    private String name;

    public ArtistEntity(@Nullable Long storageId,
                        @Nonnull String name) {
        this.storageId = storageId;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    @Nonnull
    public String getName() {
        return name;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }

    public void setName(@Nonnull String name) {
        this.name = name;
    }

}
