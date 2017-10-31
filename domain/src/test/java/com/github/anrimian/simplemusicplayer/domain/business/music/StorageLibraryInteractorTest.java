package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.business.music.utils.FakeMusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.files.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;

/**
 * Created on 25.10.2017.
 */
public class StorageLibraryInteractorTest {

    private MusicProviderRepository musicProviderRepository;
    private StorageLibraryInteractor storageLibraryInteractor;

    private List<Composition> fakeCompositions = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        Composition one = new Composition();
        one.setFilePath("root/music/one");
        fakeCompositions.add(one);

        Composition two = new Composition();
        two.setFilePath("root/music/two");
        fakeCompositions.add(two);

        Composition three = new Composition();
        three.setFilePath("root/music/old/three");
        fakeCompositions.add(three);

        Composition four = new Composition();
        four.setFilePath("root/music/old/to delete/four");
        fakeCompositions.add(four);

        musicProviderRepository = new FakeMusicProviderRepository(fakeCompositions);
        storageLibraryInteractor = new StorageLibraryInteractorImpl(musicProviderRepository);
    }

    @Test
    public void testFullTreeRootFilter() {
        List<FileSource> collection = getListMusicListInPath(null);
        assertEquals(3, collection.size());
        FileSource old = collection.get(0);
        assertEquals("old", ((FolderFileSource) old).getPath());

        String path = ((FolderFileSource) old).getPath();
        List<FileSource> oldCollection = getListMusicListInPath(path);
        FolderFileSource toDelete = (FolderFileSource) oldCollection.get(0);
        assertEquals("to delete", toDelete.getPath());

        path += "/" + toDelete.getPath();
        List<FileSource> toDeleteCollection = getListMusicListInPath(path);
        assertEquals("root/music/old/to delete/four", ((MusicFileSource) toDeleteCollection.get(0)).getComposition().getFilePath());
    }

    @Test
    public void testPathTreeFilter() {
        List<FileSource> collection = getListMusicListInPath("old");
        testCorrectCollection(collection);
        assertEquals(2, collection.size());
        assertThat(collection.get(0), instanceOf(FolderFileSource.class));
        assertThat(collection.get(1), instanceOf(MusicFileSource.class));
    }

    @Test
    public void testLastItemTreeFilter() {
        List<FileSource> collection = getListMusicListInPath("old/to delete");
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

    private List<FileSource> getListMusicListInPath(String path) {
        return storageLibraryInteractor.getMusicInPath(path).blockingGet();
    }

    @After
    public void tearDown() throws Exception {

    }

}