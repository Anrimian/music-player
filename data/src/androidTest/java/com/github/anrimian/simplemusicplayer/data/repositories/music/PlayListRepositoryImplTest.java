package com.github.anrimian.simplemusicplayer.data.repositories.music;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

/**
 * Created on 19.11.2017.
 */
public class PlayListRepositoryImplTest {

    private PlayListRepository playListRepository;

    private TestObserver<List<Composition>> testSubscriber = new TestObserver<>();
    private TestScheduler testScheduler = new TestScheduler();

    private Composition one = new Composition();
    private Composition two = new Composition();
    private Composition three = new Composition();
    private Composition four = new Composition();
    private List<Composition> fakeCompositions = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        one.setFilePath("root/music/one");
        one.setId(1);
        fakeCompositions.add(one);

        two.setFilePath("root/music/two");
        two.setId(2);
        fakeCompositions.add(two);

        three.setFilePath("root/music/old/three");
        three.setId(3);
        fakeCompositions.add(three);

        four.setFilePath("root/music/old/to delete/four");
        four.setId(4);
        fakeCompositions.add(four);

        Context appContext = InstrumentationRegistry.getTargetContext();
        AppDatabase appDatabase = Room.inMemoryDatabaseBuilder(appContext, AppDatabase.class).build();

        playListRepository = new PlayListRepositoryImpl(appDatabase, testScheduler);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void currentPlayListTest() throws Exception {
        TestObserver<List<Composition>> testObserver = playListRepository.setCurrentPlayList(fakeCompositions)
                .andThen(playListRepository.getCurrentPlayList())
                .flatMapCompletable(compositions -> playListRepository.setCurrentPlayList(fakeCompositions))
                .andThen(playListRepository.getCurrentPlayList())
                .test();

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        testObserver.assertNoErrors();
        testObserver.assertValue(fakeCompositions);
    }
}