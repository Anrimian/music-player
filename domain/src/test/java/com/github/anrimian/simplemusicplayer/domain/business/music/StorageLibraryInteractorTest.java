package com.github.anrimian.simplemusicplayer.domain.business.music;

import com.github.anrimian.simplemusicplayer.domain.business.music.utils.FakeMusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.exceptions.FileNodeNotFoundException;
import com.github.anrimian.simplemusicplayer.domain.repositories.MusicProviderRepository;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.FileTree;
import com.github.anrimian.simplemusicplayer.domain.utils.tree.visitors.CollectVisitor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.observers.TestObserver;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
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
    public void testFullTreeFilter() {
        List<Composition> collection = getListFromTree(null);
        assertEquals(fakeCompositions.size(), collection.size());
    }

    @Test
    public void testPathTreeFilter() {
        List<Composition> collection = getListFromTree("root/music/old");
        assertEquals(2, collection.size());
    }

    @Test
    public void testLastItemTreeFilter() {
        List<Composition> collection = getListFromTree("root/music/old/to delete");
        assertEquals(1, collection.size());
        assertThat(collection.get(0).getFilePath(), containsString("four"));
    }

    @Test
    public void testItemNotFoundTreeFilter() {
        TestObserver<FileTree<Composition>> subscriber = new TestObserver<>();

        storageLibraryInteractor.getAllMusicInPath("root/music/unknown")
                .subscribe(subscriber);

        //noinspection unchecked
        subscriber.assertFailure(FileNodeNotFoundException.class);
    }

    private List<Composition> getListFromTree(String path) {
        List<Composition> collection = new LinkedList<>();
        storageLibraryInteractor.getAllMusicInPath(path)
                .blockingGet()
                .accept(new CollectVisitor<>(collection));
        return collection;
    }

    @After
    public void tearDown() throws Exception {

    }

}