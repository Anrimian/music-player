package com.github.anrimian.simplemusicplayer.data.repositories.music;

import com.github.anrimian.simplemusicplayer.data.repositories.music.folders.MusicFolderDataSource;
import com.github.anrimian.simplemusicplayer.data.storage.StorageMusicDataSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.Folder;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.fakeComposition;
import static com.github.anrimian.simplemusicplayer.data.TestDataProvider.getTestFolderSingle;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MusicProviderRepositoryImplTest {

    private StorageMusicDataSource storageMusicDataSource = mock(StorageMusicDataSource.class);
    private MusicFolderDataSource musicFolderDataSource = mock(MusicFolderDataSource.class);
    private Scheduler scheduler = Schedulers.trampoline();

    private MusicProviderRepository musicProviderRepository = new MusicProviderRepositoryImpl(
            storageMusicDataSource,
            musicFolderDataSource,
            scheduler
    );

    @Test
    public void getAllCompositionsInPath() {
        Composition compositionOne = fakeComposition(1, "1");
        Composition compositionTwo = fakeComposition(2, "2");

        when(musicFolderDataSource.getCompositionsInPath(null))
                .thenReturn(getTestFolderSingle(
                        new FolderFileSource("basic", 1),
                        new MusicFileSource(compositionOne)
                ));

        when(musicFolderDataSource.getCompositionsInPath("basic"))
                .thenReturn(getTestFolderSingle(
                        new MusicFileSource(compositionTwo)
                ));

        musicProviderRepository.getAllCompositionsInPath(null)
                .test()
                .assertValue(compositions -> {
                    assertEquals(2, compositions.size());
                    assertEquals(compositionTwo, compositions.get(0));
                    assertEquals(compositionOne, compositions.get(1));
                    return true;
                });

    }

}