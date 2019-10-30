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

    @Nonnull
    private String artistKey;

    public ArtistEntity(@Nullable Long storageId,
                        @Nonnull String artistName,
                        @Nonnull String artistKey) {
        this.storageId = storageId;
        this.artistName = artistName;
        this.artistKey = artistKey;
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

    @Nonnull
    public String getArtistKey() {
        return artistKey;
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

    public void setArtistKey(@Nonnull String artistKey) {
        this.artistKey = artistKey;
    }
}
