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
    private List<Composition> currentPlayList = new ArrayList<>();

    public PlayQueueDataSource(CompositionsDao compositionsDao,
                               SettingsPreferences settingsPreferences) {
        this.compositionsDao = compositionsDao;
        this.settingsPreferences = settingsPreferences;
    }

    public void setPlayQueue(List<Composition> compositions) {
        compositionsDao.deleteCurrentPlayList();

        List<CompositionItemEntity> itemEntities = new ArrayList<>();
        for (int i = 0; i < initialPlayList.size(); i++) {
            Composition composition = initialPlayList.get(i);
            CompositionEntity compositionEntity = compositionsMapper.toCompositionEntity(composition);
            CompositionItemEntity compositionItemEntity = new CompositionItemEntity();
            compositionItemEntity.setComposition(compositionEntity);
            compositionItemEntity.setInitialPosition(initialPlayList.indexOf(composition));
            compositionItemEntity.setShuffledPosition(currentPlayList.indexOf(composition));
            itemEntities.add(compositionItemEntity);
        }
        compositionsDao.setCurrentPlayList(itemEntities);
    }

    public List<Composition> getPlayQueue() {
        return new ArrayList<>();
    }

    public Single<CurrentPlayListInfo> getPlayQueueOld() {
        return Single.fromCallable(() -> {
            List<CompositionItemEntity> compositionItemEntities = compositionsDao.getCurrentPlayList();
            Composition[] initialPlayListArray = new Composition[compositionItemEntities.size()];
            Composition[] currentPlayListArray = new Composition[compositionItemEntities.size()];
            for (CompositionItemEntity compositionItem: compositionItemEntities) {
                Composition composition = compositionsMapper.toComposition(compositionItem.getComposition());
                initialPlayListArray[compositionItem.getInitialPosition()] = composition;
                currentPlayListArray[compositionItem.getShuffledPosition()] = composition;
            }
            List<Composition> initialPlayList = asList(initialPlayListArray);
            List<Composition> currentPlayList = asList(currentPlayListArray);

            return new CurrentPlayListInfo(initialPlayList, currentPlayList);
        });
    }

    public void setRandomPlayingEnabled(boolean enabled, Composition currentComposition) {

    }

    private void shufflePlayList(boolean keepPosition) {
/*        Composition currentComposition = null;
        if (keepPosition) {
            currentComposition = currentPlayList.get(currentPlayPosition);
        }

        currentPlayList.clear();
        if (settingsPreferences.isRandomPlayingEnabled()) {
            List<Composition> playListToShuffle = new ArrayList<>(initialPlayList);

            if (currentComposition != null) {
                playListToShuffle.remove(currentPlayPosition);
                currentPlayList.add(currentComposition);
            }

            Collections.shuffle(playListToShuffle);
            currentPlayList.addAll(playListToShuffle);
        } else {
            currentPlayList.addAll(initialPlayList);
        }

        if (currentComposition != null) {
            currentPlayPosition = currentPlayList.indexOf(currentComposition);
        }
        currentPlayListSubject.onNext(currentPlayList);*/
    }
}
