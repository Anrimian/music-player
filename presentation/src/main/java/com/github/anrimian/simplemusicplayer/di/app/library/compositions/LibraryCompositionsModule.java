package com.github.anrimian.simplemusicplayer.di.app.library.compositions;

import com.github.anrimian.simplemusicplayer.domain.business.library.LibraryCompositionsInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.library.LibraryFilesInteractor;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.simplemusicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.simplemusicplayer.ui.library.compositions.LibraryCompositionsPresenter;
import com.github.anrimian.simplemusicplayer.ui.library.folders.LibraryFoldersPresenter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.simplemusicplayer.di.app.ErrorModule.STORAGE_ERROR_PARSER;
import static com.github.anrimian.simplemusicplayer.di.app.SchedulerModule.UI_SCHEDULER;

/**
 * Created on 31.10.2017.
 */

@Module
public class LibraryCompositionsModule {

    @Provides
    @Nonnull
    LibraryCompositionsPresenter libraryCompositionsPresenter(LibraryCompositionsInteractor interactor,
                                                              @Named(STORAGE_ERROR_PARSER) ErrorParser errorParser,
                                                              @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new LibraryCompositionsPresenter(interactor, errorParser, uiScheduler);
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
