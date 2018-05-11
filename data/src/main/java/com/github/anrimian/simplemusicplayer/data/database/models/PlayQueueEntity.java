package com.github.anrimian.simplemusicplayer.data.database.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.github.anrimian.simplemusicplayer.data.database.AppDatabase.CURRENT_PLAY_LIST;
import static com.github.anrimian.simplemusicplayer.data.database.AppDatabase.PLAY_QUEUE;

@Entity(tableName = PLAY_QUEUE)
public class PlayQueueEntity {

    @PrimaryKey
    private long id;

    private int position;
    private int shuffledPosition;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
