package com.github.anrimian.musicplayer.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        PlayQueueEntity.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayQueueDao playQueueDao();
}
