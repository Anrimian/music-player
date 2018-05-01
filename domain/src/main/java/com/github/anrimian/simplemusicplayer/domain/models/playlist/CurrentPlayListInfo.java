package com.github.anrimian.simplemusicplayer.domain.models.playlist;

import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 23.11.2017.
 */

public class CurrentPlayListInfo {

    @Nonnull
    private List<Composition> initialPlayList;

    @Nonnull
    private List<Composition> currentPlayList;

    public CurrentPlayListInfo(@Nonnull List<Composition> initialPlayList,
                               @Nonnull List<Composition> currentPlayList) {
        this.initialPlayList = initialPlayList;
        this.currentPlayList = currentPlayList;
    }

    @Nonnull
    public List<Composition> getInitialPlayList() {
        return initialPlayList;
    }

    @Nonnull
    public List<Composition> getCurrentPlayList() {
        return currentPlayList;
    }
}
