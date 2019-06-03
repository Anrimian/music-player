package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.composition.order.Order;

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

    Observable<Order> getFolderOrderObservable();

    Observable<Boolean> getCoversEnabledObservable();

    Observable<Boolean> getCoversInNotificationEnabledObservable();

    Observable<Boolean> getColoredNotificationEnabledObservable();

    Observable<Boolean> getCoversOnLockScreenEnabledObservable();

    void setCoversEnabled(boolean enabled);

    void setCoversInNotificationEnabled(boolean enabled);

    void setColoredNotificationEnabled(boolean enabled);

    void setCoversOnLockScreenEnabled(boolean enabled);

    Order getFolderOrder();

    Order getCompositionsOrder();

    int getSkipConstraintMillis();

    boolean isCoversEnabled();

    boolean isCoversInNotificationEnabled();

    boolean isColoredNotificationEnabled();

    boolean isCoversOnLockScreenEnabled();

    boolean isDecreaseVolumeOnAudioFocusLossEnabled();

    void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled);
}
