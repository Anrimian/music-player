package com.github.anrimian.musicplayer.data.database.entities.composition;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.github.anrimian.musicplayer.data.database.entities.albums.AlbumEntity;
import com.github.anrimian.musicplayer.data.database.entities.artist.ArtistEntity;
import com.github.anrimian.musicplayer.data.database.entities.folder.FolderEntity;
import com.github.anrimian.musicplayer.domain.models.composition.CorruptionType;
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource;
import com.github.anrimian.musicplayer.domain.utils.Objects;

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
                        childColumns = "albumId"),
                @ForeignKey(entity = FolderEntity.class,
                        parentColumns = "id",
                        childColumns = "folderId")
        },
        indices = {
                @Index("artistId"),
                @Index("albumId"),
                @Index("folderId")
        }
)
public class CompositionEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @Nullable
    private final Long artistId;

    @Nullable
    private final Long albumId;

    @Nullable
    private final Long folderId;

    @Nullable
    private final Long storageId;

    @Nullable
    private final String title;

    @Nullable
    private final Long trackNumber;

    @Nullable
    private final Long discNumber;

    @Nullable
    private final String comment;

    @Nullable
    private final String lyrics;

    @Nonnull
    private final String fileName;

    private final long duration;
    private final long size;

    @Nonnull
    private final Date dateAdded;
    @Nonnull
    private final Date dateModified;
    @androidx.annotation.NonNull
    private final Date lastScanDate;
    @androidx.annotation.NonNull
    private final Date coverModifyTime;

    @Nullable
    private final CorruptionType corruptionType;

    @androidx.annotation.NonNull
    private final InitialSource initialSource;

    public CompositionEntity(@Nullable Long artistId,
                             @Nullable Long albumId,
                             @Nullable Long folderId,
                             @Nullable String title,
                             @Nullable Long trackNumber,
                             @Nullable Long discNumber,
                             @Nullable String comment,
                             @Nullable String lyrics,
                             @Nonnull String fileName,
                             long duration,
                             long size,
                             @Nullable Long storageId,
                             @Nonnull Date dateAdded,
                             @Nonnull Date dateModified,
                             @NonNull Date lastScanDate,
                             @NonNull Date coverModifyTime,
                             @Nullable CorruptionType corruptionType,
                             @NonNull InitialSource initialSource) {
        this.artistId = artistId;
        this.albumId = albumId;
        this.folderId = folderId;
        this.storageId = storageId;
        this.title = title;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.comment = comment;
        this.lyrics = lyrics;
        this.fileName = fileName;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.lastScanDate = lastScanDate;
        this.coverModifyTime = coverModifyTime;
        this.corruptionType = corruptionType;
        this.initialSource = initialSource;

        Objects.requireNonNull(fileName);
    }

    @Nullable
    public Long getFolderId() {
        return folderId;
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
    public String getTitle() {
        return title;
    }

    @Nullable
    public Long getTrackNumber() {
        return trackNumber;
    }

    @Nullable
    public Long getDiscNumber() {
        return discNumber;
    }

    @Nullable
    public String getComment() {
        return comment;
    }

    @Nonnull
    public String getFileName() {
        return fileName;
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

    @androidx.annotation.NonNull
    public Date getLastScanDate() {
        return lastScanDate;
    }

    @androidx.annotation.NonNull
    public Date getCoverModifyTime() {
        return coverModifyTime;
    }

    @Nullable
    public CorruptionType getCorruptionType() {
        return corruptionType;
    }

    @Nullable
    public String getLyrics() {
        return lyrics;
    }

    @androidx.annotation.NonNull
    public InitialSource getInitialSource() {
        return initialSource;
    }
}
