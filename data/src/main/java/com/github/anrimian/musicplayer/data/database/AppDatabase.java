package com.github.anrimian.musicplayer.data.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.github.anrimian.musicplayer.data.database.converters.DateConverter;
import com.github.anrimian.musicplayer.data.database.converters.EnumConverter;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.play_list.PlayListDao;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.entities.composition.CompositionEntity;
import com.github.anrimian.musicplayer.data.database.entities.play_queue.PlayQueueEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntity;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;

/**
 * Created on 18.11.2017.
 */

@Database(entities = {
        PlayQueueEntity.class,
        CompositionEntity.class,
        PlayListEntity.class,
        PlayListEntryEntity.class
}, version = 3)//update version and migrate to new play queue
@TypeConverters({
        DateConverter.class,
        EnumConverter.class
})
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlayQueueDao playQueueDao();
    public abstract CompositionsDao compositionsDao();
    public abstract PlayListDao playListDao();
}
