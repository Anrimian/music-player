package com.github.anrimian.musicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.github.anrimian.musicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.entities.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.ShuffledPlayQueueEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        PlayQueueEntity.class,
        ShuffledPlayQueueEntity.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String PLAY_QUEUE = "play_queue";
    public static final String SHUFFLED_PLAY_QUEUE = "shuffled_play_queue";

    public abstract PlayQueueDao playQueueDao();
}
