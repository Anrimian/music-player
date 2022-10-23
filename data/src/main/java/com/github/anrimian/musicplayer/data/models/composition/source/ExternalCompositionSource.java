package com.github.anrimian.musicplayer.data.models.composition.source;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;

public class ExternalCompositionSource implements CompositionSource {

    private final Uri uri;
    private final String displayName;
    private final String title;
    private final String artist;
    private final String album;
    private final long duration;
    private final long size;
    private final byte[] imageBytes;

    public ExternalCompositionSource(Uri uri,
                                     String displayName,
                                     String title,
                                     String artist,
                                     String album,
                                     long duration,
                                     long size,
                                     byte[] imageBytes) {
        this.uri = uri;
        this.displayName = displayName;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.size = size;
        this.imageBytes = imageBytes;
    }

    public Uri getUri() {
        return uri;
    }

    public String getDisplayName() {
        return displayName;
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

    public long getDuration() {
        return duration;
    }

    public long getSize() {
        return size;
    }

    public byte[] getImageBytes() {
        return imageBytes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExternalCompositionSource that = (ExternalCompositionSource) o;

        return uri != null ? uri.equals(that.uri) : that.uri == null;
    }

    @Override
    public int hashCode() {
        return uri != null ? uri.hashCode() : 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "UriCompositionSource{" +
                "uri=" + uri +
                ", displayName='" + displayName + '\'' +
                ", title='" + title + '\'' +
                ", artist='" + artist + '\'' +
                ", album='" + album + '\'' +
                ", duration=" + duration +
                ", size=" + size +
                '}';
    }

    public static class Builder {
        private final Uri uri;
        private String displayName;
        private String title;
        private String artist;
        private String album;
        private long duration;
        private long size;
        private byte[] imageBytes;

        public Builder(@NonNull Uri uri) {
            this.uri = uri;
        }

        public Uri getUri() {
            return uri;
        }

        public Builder setDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setArtist(String artist) {
            this.artist = artist;
            return this;
        }

        public Builder setAlbum(String album) {
            this.album = album;
            return this;
        }

        public Builder setDuration(long duration) {
            this.duration = duration;
            return this;
        }

        public Builder setImageBytes(byte[] imageBytes) {
            this.imageBytes = imageBytes;
            return this;
        }

        public ExternalCompositionSource build() {
            return new ExternalCompositionSource(uri,
                    displayName,
                    title,
                    artist,
                    album,
                    duration,
                    size,
                    imageBytes
            );
        }
    }
}
