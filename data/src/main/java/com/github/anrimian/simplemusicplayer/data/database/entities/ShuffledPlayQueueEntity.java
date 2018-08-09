package com.github.anrimian.simplemusicplayer.data.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.github.anrimian.simplemusicplayer.data.database.AppDatabase.SHUFFLED_PLAY_QUEUE;

@Entity(tableName = SHUFFLED_PLAY_QUEUE)
public class ShuffledPlayQueueEntity {

    @PrimaryKey(autoGenerate = true)
    private long internalId;

    private long audioId;

    private int position;

    public long getInternalId() {
        return internalId;
    }

    public void setInternalId(long internalId) {
        this.internalId = internalId;
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
}
