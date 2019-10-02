package com.github.anrimian.musicplayer.di.app;

import android.content.Context;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.DatabaseManager;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDaoWrapper;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDao;
import com.github.anrimian.musicplayer.data.database.dao.play_queue.PlayQueueDaoWrapper;

import javax.annotation.Nonnull;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created on 20.11.2017.
 */

@Module
public class DbModule {

    @Provides
    @Nonnull
    @Singleton
    DatabaseManager provideDatabaseManager(Context context) {
        return new DatabaseManager(context);
    }

    @Provides
    @Nonnull
    @Singleton
    AppDatabase provideAppDatabase(DatabaseManager databaseManager) {
        return databaseManager.getAppDatabase();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayQueueDao playQueueDao(AppDatabase appDatabase) {
        return appDatabase.playQueueDao();
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionsDao compositionsDao(AppDatabase appDatabase) {
        return appDatabase.compositionsDao();
    }

    @Provides
    @Nonnull
    @Singleton
    PlayQueueDaoWrapper playQueueDaoWrapper(AppDatabase appDatabase, PlayQueueDao playQueueDao) {
        return new PlayQueueDaoWrapper(appDatabase, playQueueDao);
    }

    @Provides
    @Nonnull
    @Singleton
    CompositionsDaoWrapper compositionsDaoWrapper(AppDatabase appDatabase,
                                                  CompositionsDao compositionsDao) {
        return new CompositionsDaoWrapper(appDatabase, compositionsDao);
    }
}
