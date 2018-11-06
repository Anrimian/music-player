package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.Order;

import io.reactivex.Observable;

/**
 * Created on 14.11.2017.
 */

public interface SettingsRepository {

    void setRandomPlayingEnabled(boolean enabled);

    boolean isRandomPlayingEnabled();

    void setRepeatMode(int mode);

    int getRepeatMode();

    Observable<Integer> getRepeatModeObservable();

    void setFolderOrder(Order order);

    void setCompositionsOrder(Order order);

    Order getFolderOrder();

    Order getCompositionsOrder();
}
