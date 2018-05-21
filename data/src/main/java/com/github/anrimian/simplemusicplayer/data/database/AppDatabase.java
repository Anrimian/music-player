package com.github.anrimian.simplemusicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        PlayQueueEntity.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String PLAY_QUEUE = "play_queue";

    public abstract PlayQueueDao playQueueDao();
}
