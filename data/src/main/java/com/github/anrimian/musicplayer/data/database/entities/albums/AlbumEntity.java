package com.github.anrimian.musicplayer.data.database.entities.albums;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;

import javax.annotation.Nullable;

@Entity(tableName = "albums",
        foreignKeys = {
                @ForeignKey(entity = ArtistEntity.class,
                        parentColumns = {"id"},
                        childColumns = {"artistId"})
        },
        indices = {
                @Index("artistId")
//                @Index(value = {"artistId", "albumName"}, unique = true)//hmmm
        }
)
public class AlbumEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long artistId;

    @Nullable
    private Long storageId;

    private String albumName;

    private int firstYear;
    private int lastYear;

    public AlbumEntity(@Nullable Long artistId,
                       @Nullable Long storageId,
                       String albumName,
                       int firstYear,
                       int lastYear) {
        this.artistId = artistId;
        this.storageId = storageId;
        this.albumName = albumName;
        this.firstYear = firstYear;
        this.lastYear = lastYear;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public Long getArtistId() {
        return artistId;
    }

    public void setArtistId(long artistId) {
        this.artistId = artistId;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public void setStorageId(@Nullable Long storageId) {
        this.storageId = storageId;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public int getFirstYear() {
        return firstYear;
    }

    public void setFirstYear(int firstYear) {
        this.firstYear = firstYear;
    }

    public int getLastYear() {
        return lastYear;
    }

    public void setLastYear(int lastYear) {
        this.lastYear = lastYear;
    }
}
