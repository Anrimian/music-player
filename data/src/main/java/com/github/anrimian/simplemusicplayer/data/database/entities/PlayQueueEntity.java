package com.github.anrimian.simplemusicplayer.data.database.entities;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import static com.github.anrimian.simplemusicplayer.data.database.AppDatabase.PLAY_QUEUE;

@Entity(tableName = PLAY_QUEUE)
public class PlayQueueEntity {

    @PrimaryKey
    private long id;

    private int position;

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
}
