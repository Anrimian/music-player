package com.github.anrimian.simplemusicplayer.data.repositories.music;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;

import com.github.anrimian.simplemusicplayer.data.database.AppDatabase;
import com.github.anrimian.simplemusicplayer.data.repositories.playlist.PlayListRepositoryImpl;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;
import com.github.anrimian.simplemusicplayer.domain.repositories.PlayListRepository;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;

/**
 * Created on 19.11.2017.
 */
public class PlayListRepositoryImplTest {

    private PlayListRepository playListRepository;

    private TestScheduler testScheduler = new TestScheduler();

    private Composition one = new Composition();
    private Composition two = new Composition();
    private Composition three = new Composition();
    private Composition four = new Composition();
    private List<Composition> fakeCompositions = new ArrayList<>();
    private List<Composition> shuffledCompositions = new ArrayList<>();

    private CurrentPlayListInfo currentPlayListInfo;

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

        shuffledCompositions.add(four);
        shuffledCompositions.add(three);
        shuffledCompositions.add(two);
        shuffledCompositions.add(one);
        currentPlayListInfo = new CurrentPlayListInfo(fakeCompositions, shuffledCompositions);

        Context appContext = InstrumentationRegistry.getTargetContext();
        AppDatabase appDatabase = Room.inMemoryDatabaseBuilder(appContext, AppDatabase.class).build();

        playListRepository = new PlayListRepositoryImpl(appDatabase, testScheduler);
    }

    @Test
    public void currentPlayListTest() throws Exception {
        TestObserver<CurrentPlayListInfo> testObserver = playListRepository.setCurrentPlayList(currentPlayListInfo)
                .andThen(playListRepository.getCurrentPlayList())
                .flatMapCompletable(compositions -> playListRepository.setCurrentPlayList(currentPlayListInfo))
                .andThen(playListRepository.getCurrentPlayList())
                .test();

        testScheduler.advanceTimeBy(10, TimeUnit.SECONDS);

        testObserver.assertNoErrors();
        testObserver.assertValue(currentPlayListInfo -> {
            List<Composition> initialPlayList = currentPlayListInfo.getInitialPlayList();
            List<Composition> currentPlayList = currentPlayListInfo.getCurrentPlayList();
            assertEquals(fakeCompositions, initialPlayList);
            assertEquals(shuffledCompositions, currentPlayList);
            return true;
        });
    }
}