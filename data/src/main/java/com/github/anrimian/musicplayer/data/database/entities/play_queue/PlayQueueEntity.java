package com.github.anrimian.musicplayer.data.database.entities.play_queue;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "play_queue")
public class PlayQueueEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long audioId;

    private int position;
    private int shuffledPosition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getShuffledPosition() {
        return shuffledPosition;
    }

    public void setShuffledPosition(int shuffledPosition) {
        this.shuffledPosition = shuffledPosition;
    }
}
