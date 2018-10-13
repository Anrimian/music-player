package com.github.anrimian.musicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

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
