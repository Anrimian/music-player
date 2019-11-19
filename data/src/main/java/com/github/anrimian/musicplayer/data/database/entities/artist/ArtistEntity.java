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
    private String artistName;

    public ArtistEntity(@Nullable Long storageId,
                        @Nonnull String artistName) {
        this.storageId = storageId;
        this.artistName = artistName;
    }

    public long getId() {
        return id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    @Nonnull
    public String getArtistName() {
        return artistName;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }

    public void setArtistName(@Nonnull String artistName) {
        this.artistName = artistName;
    }

}
