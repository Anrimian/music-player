package com.github.anrimian.musicplayer.data.database.entities.composition;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;

import java.util.Date;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Entity(tableName = "compositions")
public class CompositionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

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
    private long mediaStoreId;

    @Nonnull
    private Date dateAdded;
    @Nonnull
    private Date dateModified;

    @Nullable
    private CorruptionType corruptionType;

    public CompositionEntity(long id,
                             @Nullable String artist,
                             @Nullable String title,
                             @Nullable String album,
                             @Nonnull String filePath,
                             long duration,
                             long size,
                             long mediaStoreId,
                             @Nonnull Date dateAdded,
                             @Nonnull Date dateModified,
                             @Nullable CorruptionType corruptionType) {
        this.id = id;
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.filePath = filePath;
        this.duration = duration;
        this.size = size;
        this.mediaStoreId = mediaStoreId;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.corruptionType = corruptionType;
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

    public long getMediaStoreId() {
        return mediaStoreId;
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
    public CorruptionType isCorrupted() {
        return corruptionType;
    }
}
