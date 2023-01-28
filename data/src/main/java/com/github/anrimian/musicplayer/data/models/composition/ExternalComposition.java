package com.github.anrimian.musicplayer.data.models.composition;

import androidx.annotation.Nullable;

public class ExternalComposition {

    private final String parentPath;
    private final String fileName;
    @Nullable
    private final String title;
    @Nullable
    private final String artist;
    @Nullable
    private final String album;
    @Nullable
    private final String albumArtist;
    @Nullable
    private final Long trackNumber;
    @Nullable
    private final Long discNumber;
    @Nullable
    private final String comment;
    @Nullable
    private final String lyrics;

    private final long duration;
    private final long size;
    private final long dateAdded;
    private final long dateModified;
    private final long coverModifyTime;

    private final boolean isFileExists;

    public ExternalComposition(String parentPath,
                               String fileName,
                               @Nullable String title,
                               @Nullable String artist,
                               @Nullable String album,
                               @Nullable String albumArtist,
                               @Nullable Long trackNumber,
                               @Nullable Long discNumber,
                               @Nullable String comment,
                               @Nullable String lyrics,
                               long duration,
                               long size,
                               long dateAdded,
                               long dateModified,
                               long coverModifyTime,
                               boolean isFileExists) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumArtist = albumArtist;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.comment = comment;
        this.lyrics = lyrics;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.coverModifyTime = coverModifyTime;
        this.isFileExists = isFileExists;
    }

    public String getParentPath() {
        return parentPath;
    }

    public String getFileName() {
        return fileName;
    }

    @Nullable
    public String getTitle() {
        return title;
    }

    @Nullable
    public String getArtist() {
        return artist;
    }

    @Nullable
    public String getAlbum() {
        return album;
    }

    @Nullable
    public String getAlbumArtist() {
        return albumArtist;
    }

    public long getDateModified() {
        return dateModified;
    }

    public long getCoverModifyTime() {
        return coverModifyTime;
    }

    public boolean isFileExists() {
        return isFileExists;
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

    @Nullable
    public String getLyrics() {
        return lyrics;
    }

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public long getDateAdded() {
        return dateAdded;
    }
}
