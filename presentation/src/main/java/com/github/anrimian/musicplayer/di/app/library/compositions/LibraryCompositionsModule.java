package com.github.anrimian.musicplayer.di.app.library.compositions;

import com.github.anrimian.musicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.playlists.PlayListsInteractor;
import com.github.anrimian.musicplayer.domain.business.settings.DisplaySettingsInteractor;
import com.github.anrimian.musicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.ErrorModule.STORAGE_ERROR_PARSER;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 31.10.2017.
 */

@Module
public class LibraryCompositionsModule {

    @Provides
    @Nonnull
    LibraryCompositionsPresenter libraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                                              PlayListsInteractor playListsInteractor,
                                                              MusicPlayerInteractor playerInteractor,
                                                              DisplaySettingsInteractor displaySettingsInteractor,
                                                              @Named(STORAGE_ERROR_PARSER) ErrorParser errorParser,
                                                              @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LibraryCompositionsPresenter(interactor,
                playListsInteractor,
                playerInteractor,
                displaySettingsInteractor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    LibraryCompositionsInteractor libraryCompositionsInteractor(MusicProviderRepository musicProviderRepository,
                                                                MusicPlayerInteractor musicPlayerInteractor,
                                                                SettingsRepository settingsRepository) {
        return new LibraryCompositionsInteractor(musicProviderRepository,
                musicPlayerInteractor,
                settingsRepository);
    }
}
