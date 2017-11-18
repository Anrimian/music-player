package com.github.anrimian.simplemusicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {CompositionEntity.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String CURRENT_PLAY_LIST = "current_play_list";

    public abstract PlayQueueDao playQueueDao();
}
