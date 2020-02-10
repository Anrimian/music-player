package com.github.anrimian.musicplayer.di.app.library.files;

import com.github.anrimian.musicplayer.domain.business.library.LibraryFoldersInteractor;
import com.github.anrimian.musicplayer.domain.business.library.LibraryFoldersScreenInteractor;
import com.github.anrimian.musicplayer.domain.repositories.EditorRepository;
import com.github.anrimian.musicplayer.domain.repositories.LibraryRepository;
import com.github.anrimian.musicplayer.domain.repositories.MediaScannerRepository;
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser;
import com.github.anrimian.musicplayer.ui.library.folders.root.FolderRootPresenter;

import javax.annotation.Nonnull;
import javax.inject.Named;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;

import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;

@Module
public class LibraryFilesModule {

    @Provides
    @Nonnull
    FolderRootPresenter folderRootPresenter(LibraryFoldersInteractor interactor,
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
                                                          EditorRepository editorRepository,
                                                          MediaScannerRepository mediaScannerRepository) {
        return new LibraryFoldersScreenInteractor(foldersInteractor,
                libraryRepository,
                editorRepository,
                mediaScannerRepository);
    }

}
