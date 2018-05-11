package com.github.anrimian.simplemusicplayer.data.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.database.dao.PlayQueueDao;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionItemEntity;
import com.github.anrimian.simplemusicplayer.data.database.models.PlayQueueEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        CompositionEntity.class,
        CompositionItemEntity.class,
        PlayQueueEntity.class
}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public static final String COMPOSITIONS = "compositions";
    public static final String CURRENT_PLAY_LIST = "current_play_list";

    public static final String PLAY_QUEUE = "play_queue";

    public abstract CompositionsDao compositionsDao();
    public abstract PlayQueueDao playQueueDao();
}
