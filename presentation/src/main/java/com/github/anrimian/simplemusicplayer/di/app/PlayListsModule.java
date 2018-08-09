package com.github.anrimian.simplemusicplayer.di.app;

import android.content.Context;

import com.github.anrimian.simplemusicplayer.data.repositories.playlists.PlayListsRepositoryImpl;
import com.github.anrimian.simplemusicplayer.data.storage.providers.playlists.StoragePlayListDataSource;
import com.github.anrimian.simplemusicplayer.data.storage.providers.playlists.StoragePlayListsProvider;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListsRepository;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.choose.ChoosePlayListPresenter;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.create.CreatePlayListPresenter;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlist.PlayListPresenter;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.PlayListsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.DB_SCHEDULER;
import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class PlayListsModule {

    @Provides
    @Nonnull
    PlayListsPresenter playListsPresenter(PlayListsInteractor playListsInteractor,
                                          @Named(UI_SCHEDULER) Scheduler uiSchedule) {
        return new PlayListsPresenter(playListsInteractor, uiSchedule);
    }

    @Provides
    @Nonnull
    ChoosePlayListPresenter choosePlayListPresenter(PlayListsInteractor playListsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiSchedule) {
        return new ChoosePlayListPresenter(playListsInteractor, uiSchedule);
    }

    @Provides
    @Nonnull
    CreatePlayListPresenter createPlayListPresenter(PlayListsInteractor playListsInteractor,
                                                    @Named(UI_SCHEDULER) Scheduler uiSchedule,
                                                    ErrorParser errorParser) {
        return new CreatePlayListPresenter(playListsInteractor, uiSchedule, errorParser);
    }

    @Provides
    @Nonnull
    PlayListsInteractor playListsInteractor(PlayListsRepository playListsRepository) {
        return new PlayListsInteractor(playListsRepository);
    }

    @Provides
    @Nonnull
    @Singleton
    PlayListsRepository playListsRepository(StoragePlayListDataSource storagePlayListDataSource,
                                            @Named(DB_SCHEDULER) Scheduler scheduler) {
        return new PlayListsRepositoryImpl(storagePlayListDataSource, scheduler);
    }

    @Provides
    @Nonnull
    @Singleton
    StoragePlayListDataSource storagePlayListDataSource(
            StoragePlayListsProvider storagePlayListDataSource) {
        return new StoragePlayListDataSource(storagePlayListDataSource);
    }

    @Provides
    @Nonnull
    StoragePlayListsProvider storagePlayListsProvider(Context context) {
        return new StoragePlayListsProvider(context);
    }

}
