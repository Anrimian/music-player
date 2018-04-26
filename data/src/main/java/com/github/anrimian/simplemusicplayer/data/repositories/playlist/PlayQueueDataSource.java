package com.github.anrimian.simplemusicplayer.data.repositories.playlist;

import com.github.anrimian.simplemusicplayer.data.database.dao.CompositionsDao;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionEntity;
import com.github.anrimian.simplemusicplayer.data.database.models.CompositionItemEntity;
import com.github.anrimian.simplemusicplayer.data.models.compositions.CompositionsMapper;
import com.github.anrimian.simplemusicplayer.data.preferences.SettingsPreferences;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.CurrentPlayListInfo;

import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Single;

import static java.util.Arrays.asList;

/**
 * Created on 16.04.2018.
 */
public class PlayQueueDataSource {

    private final CompositionsMapper compositionsMapper = Mappers.getMapper(CompositionsMapper.class);

    private final CompositionsDao compositionsDao;
    private final SettingsPreferences settingsPreferences;

    private List<Composition> initialPlayList;
    private List<Composition> shuffledPlayList;

    public PlayQueueDataSource(CompositionsDao compositionsDao,
                               SettingsPreferences settingsPreferences) {
        this.compositionsDao = compositionsDao;
        this.settingsPreferences = settingsPreferences;
    }

    public void setPlayQueue(List<Composition> compositions) {
        initialPlayList = compositions;
        shuffledPlayList = new ArrayList<>(initialPlayList);
        Collections.shuffle(shuffledPlayList);

        savePlayQueue();
    }

    public List<Composition> getPlayQueue() {
        if (initialPlayList == null) {
            loadPlayQueue();
        }

        if (settingsPreferences.isRandomPlayingEnabled()) {
            return shuffledPlayList;
        }
        return initialPlayList;
    }

    /**
     *
     * @return new position of current composition
     */
    public int setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {
        if (initialPlayList == null) {
            throw new IllegalStateException("change play mode before initialization");
        }

        settingsPreferences.setRandomPlayingEnabled(enabled);
        if (enabled) {
            shuffledPlayList.remove(currentComposition);
            shuffledPlayList.add(0, currentComposition);
            savePlayQueue();//TODO optimize in next refactoring wave
            return 0;
        }
        return initialPlayList.indexOf(currentComposition);
    }

    private void savePlayQueue() {
        compositionsDao.deleteCurrentPlayList();

        List<CompositionItemEntity> itemEntities = new ArrayList<>();
        for (int i = 0; i < initialPlayList.size(); i++) {
            Composition composition = initialPlayList.get(i);
            CompositionEntity compositionEntity = compositionsMapper.toCompositionEntity(composition);
            CompositionItemEntity compositionItemEntity = new CompositionItemEntity();
            compositionItemEntity.setComposition(compositionEntity);
            compositionItemEntity.setInitialPosition(initialPlayList.indexOf(composition));
            compositionItemEntity.setShuffledPosition(shuffledPlayList.indexOf(composition));
            itemEntities.add(compositionItemEntity);
        }
        compositionsDao.setCurrentPlayList(itemEntities);
    }

    private void loadPlayQueue() {
        List<CompositionItemEntity> compositionItemEntities = compositionsDao.getCurrentPlayList();
        Composition[] initialPlayListArray = new Composition[compositionItemEntities.size()];
        Composition[] currentPlayListArray = new Composition[compositionItemEntities.size()];
        for (CompositionItemEntity compositionItem: compositionItemEntities) {
            Composition composition = compositionsMapper.toComposition(compositionItem.getComposition());
            initialPlayListArray[compositionItem.getInitialPosition()] = composition;
            currentPlayListArray[compositionItem.getShuffledPosition()] = composition;
        }
        initialPlayList = asList(initialPlayListArray);
        shuffledPlayList = asList(currentPlayListArray);
    }
}
