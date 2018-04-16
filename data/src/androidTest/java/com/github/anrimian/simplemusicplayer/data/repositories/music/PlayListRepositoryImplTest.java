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
import java.util.Collections;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;

/**
 * Created on 19.11.2017.
 */
public class PlayListRepositoryImplTest {

    private PlayListRepository playQueueRepository;

    private TestScheduler testScheduler = new TestScheduler();

    /*private Composition one = new Composition();
    private Composition two = new Composition();
    private Composition three = new Composition();
    private Composition four = new Composition();*/
    private List<Composition> fakeCompositions = new ArrayList<>();
    private List<Composition> shuffledCompositions;

    private CurrentPlayListInfo currentPlayListInfo;

    @Before
    public void setUp() throws Exception {
        for (int i = 0; i < 100000; i++) {
            Composition composition = new Composition();

            composition.setFilePath("music-" + i);
            composition.setId(i);
            fakeCompositions.add(composition);
        }
        shuffledCompositions = new ArrayList<>(fakeCompositions);
        Collections.shuffle(shuffledCompositions);

        currentPlayListInfo = new CurrentPlayListInfo(fakeCompositions, shuffledCompositions);

        Context appContext = InstrumentationRegistry.getTargetContext();
        AppDatabase appDatabase = Room.databaseBuilder(appContext, AppDatabase.class, "test_db").build();

        playQueueRepository = new PlayListRepositoryImpl(appDatabase, Schedulers.computation());
    }

    @Test
    public void currentPlayListTest() throws Exception {
        /*TestObserver<CurrentPlayListInfo> testObserver =*/ playQueueRepository.setCurrentPlayList(currentPlayListInfo)
                .andThen(playQueueRepository.getCurrentPlayList())
                .flatMapCompletable(compositions -> playQueueRepository.setCurrentPlayList(currentPlayListInfo))
                .andThen(playQueueRepository.getCurrentPlayList())
                .subscribe(currentPlayListInfo -> {
                    List<Composition> initialPlayList = currentPlayListInfo.getInitialPlayList();
                    List<Composition> currentPlayList = currentPlayListInfo.getCurrentPlayList();
                    assertEquals(fakeCompositions, initialPlayList);
                    assertEquals(shuffledCompositions, currentPlayList);
                });

/*        testScheduler.advanceTimeBy(60, TimeUnit.SECONDS);

        testObserver.assertNoErrors();
        testObserver.assertValue(currentPlayListInfo -> {
            List<Composition> initialPlayList = currentPlayListInfo.getInitialPlayList();
            List<Composition> currentPlayList = currentPlayListInfo.getPlayQueue();
            assertEquals(fakeCompositions, initialPlayList);
            assertEquals(shuffledCompositions, currentPlayList);
            return true;
        });*/
    }

    @Test
    public void setPlayQueue() {
    }
}