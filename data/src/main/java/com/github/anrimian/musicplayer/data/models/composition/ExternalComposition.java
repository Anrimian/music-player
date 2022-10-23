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
    private final String lyrics;


    private final int albumFirstYear;
    private final int albumLastYear;

    private final long duration;
    private final long size;
    private final long dateAdded;
    private final long dateModified;
    private final long lastScanDate;

    private final int audioFileType;

    private final boolean isFileExists;

    public ExternalComposition(String parentPath,
                               String fileName,
                               @Nullable String title,
                               @Nullable String artist,
                               @Nullable String album,
                               @Nullable String albumArtist,
                               @Nullable String lyrics,
                               int albumFirstYear,
                               int albumLastYear,
                               long duration,
                               long size,
                               int audioFileType,
                               long dateAdded,
                               long dateModified,
                               long lastScanDate,
                               boolean isFileExists) {
        this.parentPath = parentPath;
        this.fileName = fileName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumArtist = albumArtist;
        this.lyrics = lyrics;
        this.albumFirstYear = albumFirstYear;
        this.albumLastYear = albumLastYear;
        this.duration = duration;
        this.size = size;
        this.audioFileType = audioFileType;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified;
        this.lastScanDate = lastScanDate;
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

    public int getAlbumFirstYear() {
        return albumFirstYear;
    }

    public int getAlbumLastYear() {
        return albumLastYear;
    }

    public long getDateModified() {
        return dateModified;
    }

    public boolean isFileExists() {
        return isFileExists;
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

    public long getLastScanDate() {
        return lastScanDate;
    }

    public int getAudioFileType() {
        return audioFileType;
    }
}
