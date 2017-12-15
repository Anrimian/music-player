package com.github.anrimian.simplemusicplayer.domain.business.library;

import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created on 25.10.2017.
 */
public class StorageLibraryInteractorTest {

    private MusicProviderRepository musicProviderRepository;
    private StorageLibraryInteractor storageLibraryInteractor;
    private MusicPlayerInteractor musicPlayerInteractor;

    private List<Composition> fakeCompositions = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Composition one = new Composition();
        one.setFilePath("root/music/one");
        one.setDuration(100);
        fakeCompositions.add(one);

        Composition two = new Composition();
        two.setFilePath("root/music/two");
        two.setDuration(100);
        fakeCompositions.add(two);

        Composition three = new Composition();
        three.setFilePath("root/music/old/three");
        three.setDuration(100);
        fakeCompositions.add(three);

        Composition four = new Composition();
        four.setFilePath("root/music/old/to delete/four");
        four.setDuration(100);
        fakeCompositions.add(four);

        musicPlayerInteractor = mock(MusicPlayerInteractor.class);

        musicProviderRepository = mock(MusicProviderRepository.class);
        when(musicProviderRepository.getAllCompositions()).thenReturn(Single.just(fakeCompositions));

        storageLibraryInteractor = new StorageLibraryInteractor(musicProviderRepository, musicPlayerInteractor);
    }

    @Test
    public void testFullTreeRootFilter() {
        List<FileSource> collection = getMusicListInPath(null);
        assertEquals(3, collection.size());
        FolderFileSource old = (FolderFileSource) collection.get(0);
        assertEquals("music/old", old.getPath());
        assertEquals(2, old.getFilesCount());

        List<FileSource> oldCollection = getMusicListInPath(old.getPath());
        FolderFileSource toDelete = (FolderFileSource) oldCollection.get(0);
        assertEquals("music/old/to delete", toDelete.getPath());

        List<FileSource> toDeleteCollection = getMusicListInPath(toDelete.getPath());
        assertEquals("root/music/old/to delete/four", ((MusicFileSource) toDeleteCollection.get(0)).getComposition().getFilePath());
    }

    @Test
    public void testPathTreeFilter() {
        List<FileSource> collection = getMusicListInPath("root/music/old");
        testCorrectCollection(collection);
        assertEquals(2, collection.size());
        assertThat(collection.get(0), instanceOf(FolderFileSource.class));
        assertThat(collection.get(1), instanceOf(MusicFileSource.class));
    }

    @Test
    public void testLastItemTreeFilter() {
        List<FileSource> collection = getMusicListInPath("root/music/old/to delete");
        assertEquals(1, collection.size());
        assertEquals("root/music/old/to delete/four", ((MusicFileSource) collection.get(0)).getComposition().getFilePath());
    }

    @Test
    public void testItemNotFoundTreeFilter() {
        TestObserver<List<FileSource>> subscriber = new TestObserver<>();

        storageLibraryInteractor.getMusicInPath("root/music/unknown")
                .subscribe(subscriber);

        //noinspection unchecked
        subscriber.assertFailure(FileNodeNotFoundException.class);
    }

    @Test
    public void playAllMusicInPathTest() {
        storageLibraryInteractor.playAllMusicInPath(null);

        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        verify(musicPlayerInteractor).startPlaying(captor.capture());
        assertEquals(4, captor.getValue().size());
    }

    private void testCorrectCollection(List<FileSource> collection) {
        for (FileSource fileSource: collection) {
            if (fileSource instanceof MusicFileSource) {
                assertNotNull(((MusicFileSource) fileSource).getComposition());
                break;
            }
            if (fileSource instanceof FolderFileSource) {
                assertNotNull(((FolderFileSource) fileSource).getPath());
                break;
            }
        }
    }

    private List<FileSource> getMusicListInPath(String path) {
        return storageLibraryInteractor.getMusicInPath(path).blockingGet();
    }

    @After
    public void tearDown() throws Exception {

    }

}