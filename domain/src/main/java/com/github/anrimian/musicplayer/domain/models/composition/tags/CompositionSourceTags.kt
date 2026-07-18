package com.github.anrimian.musicplayer.domain.models.composition.tags;

public class CompositionSourceTags {
    private final String title;
    private final String artist;
    private final String album;
    private final String albumArtist;
    private final int durationSeconds;
    private final Long trackNumber;
    private final Long discNumber;
    private final String comment;
    private final String lyrics;
    private final String[] genres;

    public CompositionSourceTags(String title,
                                 String artist,
                                 String album,
                                 String albumArtist,
                                 int durationSeconds,
                                 Long trackNumber,
                                 Long discNumber,
                                 String comment,
                                 String lyrics,
                                 String[] genres) {
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.albumArtist = albumArtist;
        this.durationSeconds = durationSeconds;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.comment = comment;
        this.lyrics = lyrics;
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getAlbum() {
        return album;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public int getDurationSeconds() {
        return durationSeconds;
    }

    public Long getTrackNumber() {
        return trackNumber;
    }

    public Long getDiscNumber() {
        return discNumber;
    }

    public String getComment() {
        return comment;
    }

    public String getLyrics() {
        return lyrics;
    }

    public String[] getGenres() {
        return genres;
    }
}
