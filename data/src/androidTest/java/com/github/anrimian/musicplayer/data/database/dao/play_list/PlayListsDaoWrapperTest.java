package com.github.anrimian.musicplayer.data.database.dao.play_list;

import android.content.Context;
import android.util.Log;

import androidx.core.util.Pair;
import androidx.room.Room;
import androidx.test.platform.app.InstrumentationRegistry;

import com.github.anrimian.musicplayer.data.database.AppDatabase;
import com.github.anrimian.musicplayer.data.database.dao.compositions.CompositionsDao;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryDto;
import com.github.anrimian.musicplayer.data.database.entities.playlist.PlayListEntryEntity;
import com.github.anrimian.musicplayer.data.models.changes.Change;
import com.github.anrimian.musicplayer.data.storage.providers.playlists.StoragePlayList;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;
import static java.util.Collections.emptyList;
import static utils.TestDataProvider.composition;

public class PlayListsDaoWrapperTest {

    private PlayListDao playListDao;
    private CompositionsDao compositionsDao;
    private AppDatabase db;

    private PlayListsDaoWrapper daoWrapper;

    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        compositionsDao = db.compositionsDao();
        playListDao = db.playListDao();

        daoWrapper = new PlayListsDaoWrapper(playListDao, compositionsDao, db);
    }

    @After
    public void tearDown() {
        db.close();
    }

    @Test
    public void testModifyChangeWithDuplicates() {
        StoragePlayList playList1 = new StoragePlayList(1L,
                "test",
                new Date(),
                new Date());
        StoragePlayList playList2 = new StoragePlayList(2L,
                "test1",
                new Date(),
                new Date());
        daoWrapper.applyChanges(asList(new Pair<>(playList1, emptyList()), new Pair<>(playList2, emptyList())),
                emptyList());
        StoragePlayList duplicatePlayList = new StoragePlayList(2L,
                "test",
                new Date(),
                new Date());
        daoWrapper.applyChanges(emptyList(), asList(new Change<>(playList1, duplicatePlayList)));
    }

    @Test
    public void testMoveItems() {
        long playlistId = daoWrapper.insertPlayList("playlist", new Date(), new Date());

        for (int i = 0; i < 10; i++) {
            long id = compositionsDao.insert(composition(null, null, String.valueOf(i)));
            playListDao.insertPlayListEntries(asList(new PlayListEntryEntity(
                    null,
                    id,
                    playlistId,
                    i
            )));
        }

        List<PlayListEntryDto> items = playListDao.getPlayListItemsObservable(playlistId)
                .blockingFirst();
        displayItems("testMoveItems, items: ", items);

        daoWrapper.moveItems(playlistId, 0, 7);

        items = playListDao.getPlayListItemsObservable(playlistId)
                .blockingFirst();
        displayItems("testMoveItems, moved items: ", items);

    }

    @Test
    public void testUpdatePlaylistNameThatAlreadyExists() {
        Date date = new Date();
        StoragePlayList playList1 = new StoragePlayList(1L, "test", date, date);
        StoragePlayList playList2 = new StoragePlayList(2L, "test1", date, date);
        StoragePlayList playList3 = new StoragePlayList(3L, "test2", date, date);
        daoWrapper.insertPlayList(playList1);
        daoWrapper.insertPlayList(playList2);
        daoWrapper.insertPlayList(playList3);

        StoragePlayList duplicatePlayList3 = new StoragePlayList(3L, "test", date, date);
        daoWrapper.applyChanges(emptyList(), asList(new Change<>(playList3, duplicatePlayList3)));

        System.out.println("KEKAS" + daoWrapper.getPlayListsObservable().blockingFirst());
    }

    private void displayItems(String message, List<PlayListEntryDto> items) {
        StringBuilder sb = new StringBuilder();
        for (PlayListEntryDto item : items) {
            sb.append("\n");
            sb.append("itemId = ");
            sb.append(item.getItemId());
            sb.append("; title = ");
            sb.append(item.getComposition().getTitle());
            sb.append(";");
        }
        String text = sb.toString();
        Log.d("KEK", message + text);
        System.out.println(message + text);
    }
}