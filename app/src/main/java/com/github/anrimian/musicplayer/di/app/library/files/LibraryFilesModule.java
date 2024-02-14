package com.github.anrimian.musicplayer.di.app.library.files;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.interactors.library.LibraryFoldersScreenInteractor;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.folders.root.FolderRootPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.rxjava3.core.Scheduler;

@Module
public class LibraryFilesModule {

    @Provides
    @Nonnull
    FolderRootPresenter folderRootPresenter(LibraryFoldersScreenInteractor interactor,
                                            ErrorParser errorParser,
                                            @Named(UI_SCHEDULER) Scheduler uiScheduler) {
        return new FolderRootPresenter(interactor,
                errorParser,
                uiScheduler);
    }

    @Provides
    @Nonnull
    @LibraryFilesScope
    LibraryFoldersScreenInteractor libraryFilesInteractor(LibraryFoldersInteractor foldersInteractor,
                                                          LibraryRepository libraryRepository,
                                                          UiStateRepository uiStateRepository) {
        return new LibraryFoldersScreenInteractor(foldersInteractor,
                libraryRepository,
                uiStateRepository);
    }

}
