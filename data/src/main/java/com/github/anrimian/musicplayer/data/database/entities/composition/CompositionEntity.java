package com.github.anrimian.musicplayer.data.database.entities.composition;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "compositions",
        foreignKeys = {
                @ForeignKey(entity = ArtistEntity.class,
                        parentColumns = "id",
                        childColumns = "artistId"),
                @ForeignKey(entity = AlbumEntity.class,
                        parentColumns = "id",
                        childColumns = "albumId")
        },
        indices = {
                @Index("artistId"),
                @Index("albumId")
        }
)
public class CompositionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private Long artistId;

    @Nullable
    private Long albumId;

    @Nullable
    private Long storageId;

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

    @Nonnull
    private Date dateAdded;
    @Nonnull
    private Date dateModified;

    @Nullable
    private CorruptionType corruptionType;

    public CompositionEntity(@Nullable Long artistId,
                             @Nullable Long albumId,
                             @Nullable String artist,
                             @Nullable String title,
                             @Nullable String album,
                             @Nonnull String filePath,
                             long duration,
                             long size,
                             @Nullable Long storageId,
                             @Nonnull Date dateAdded,
                             @Nonnull Date dateModified,
                             @Nullable CorruptionType corruptionType) {
        this.artistId = artistId;
        this.albumId = albumId;
        this.storageId = storageId;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;
    }

    @Nullable
    public Long getAlbumId() {
        return albumId;
    }

    @Nullable
    public Long getArtistId() {
        return artistId;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Nullable
    public Long getStorageId() {
        return storageId;
    }

    public long getId() {
        return id;
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

    @Nonnull
    public Date getDateAdded() {
        return dateAdded;
    }

    @Nonnull
    public Date getDateModified() {
        return dateModified;
    }

    @Nullable
    public CorruptionType getCorruptionType() {
        return corruptionType;
    }
}
