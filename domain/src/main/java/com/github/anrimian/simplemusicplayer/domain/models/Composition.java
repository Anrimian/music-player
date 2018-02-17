package com.github.anrimian.simplemusicplayer.domain.models;

import java.util.Date;

import javax.annotation.Nullable;

/**
 * Created on 24.10.2017.
 */

public class Composition {

    @Nullable
    private String artist;
    private String title;
    private String album;
    private String filePath;
    private String composer;
    private String displayName;

    private long duration;
    private long size;
    private long id;

    private Date dateAdded;
    private Date dateModified;

    private boolean isAlarm;
    private boolean isMusic;
    private boolean isNotification;
    private boolean isPodcast;
    private boolean isRingtone;

    @Nullable
    private Integer year;

    @Nullable
    public String getArtist() {
        return artist;
    }

    public void setArtist(@Nullable String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Date getDateModified() {
        return dateModified;
    }

    public void setDateModified(Date dateModified) {
        this.dateModified = dateModified;
    }

    public boolean isAlarm() {
        return isAlarm;
    }

    public void setAlarm(boolean alarm) {
        isAlarm = alarm;
    }

    public boolean isMusic() {
        return isMusic;
    }

    public void setMusic(boolean music) {
        isMusic = music;
    }

    public boolean isNotification() {
        return isNotification;
    }

    public void setNotification(boolean notification) {
        isNotification = notification;
    }

    public boolean isPodcast() {
        return isPodcast;
    }

    public void setPodcast(boolean podcast) {
        isPodcast = podcast;
    }

    public boolean isRingtone() {
        return isRingtone;
    }

    public void setRingtone(boolean ringtone) {
        isRingtone = ringtone;
    }

    @Nullable
    public Integer getYear() {
        return year;
    }

    public void setYear(@Nullable Integer year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return "Composition{" +
                "\n artist='" + artist + '\'' +
                "\n title='" + title + '\'' +
                "\n album='" + album + '\'' +
                "\n filePath='" + filePath + '\'' +
                "\n composer='" + composer + '\'' +
                "\n displayName='" + displayName + '\'' +
                "\n duration=" + duration +
                "\n size=" + size +
                "\n id=" + id +
                "\n dateAdded=" + dateAdded +
                "\n dateModified=" + dateModified +
                "\n isAlarm=" + isAlarm +
                "\n isMusic=" + isMusic +
                "\n isNotification=" + isNotification +
                "\n isPodcast=" + isPodcast +
                "\n isRingtone=" + isRingtone +
                "\n year=" + year +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Composition that = (Composition) o;

        return id == that.id;

    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }
}
