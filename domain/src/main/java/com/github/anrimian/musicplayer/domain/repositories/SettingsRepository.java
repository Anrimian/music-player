package com.github.anrimian.musicplayer.domain.repositories;

import com.github.anrimian.musicplayer.domain.models.order.Order;

import io.reactivex.rxjava3.core.Observable;

/**
 * Created on 14.11.2017.
 */

public interface SettingsRepository {

    void setRandomPlayingEnabled(boolean enabled);

    boolean isRandomPlayingEnabled();

    Observable<Boolean> getRandomPlayingObservable();

    void setRepeatMode(int mode);

    int getRepeatMode();

    Observable<Integer> getRepeatModeObservable();

    void setFolderOrder(Order order);

    void setCompositionsOrder(Order order);

    Observable<Order> getFolderOrderObservable();

    Observable<Boolean> getCoversEnabledObservable();

    Observable<Boolean> getCoversInNotificationEnabledObservable();

    Observable<Boolean> getColoredNotificationEnabledObservable();

    Observable<Boolean> getNotificationCoverStubEnabledObservable();

    Observable<Boolean> getCoversOnLockScreenEnabledObservable();

    void setCoversEnabled(boolean enabled);

    void setCoversInNotificationEnabled(boolean enabled);

    void setColoredNotificationEnabled(boolean enabled);

    void setNotificationCoverStubEnabled(boolean enabled);

    void setCoversOnLockScreenEnabled(boolean enabled);

    Order getFolderOrder();

    Order getCompositionsOrder();

    Order getArtistsOrder();

    void setArtistsOrder(Order order);

    Observable<Order> getArtistsOrderObservable();

    Order getAlbumsOrder();

    void setAlbumsOrder(Order order);

    Observable<Order> getAlbumsOrderObservable();

    Order getGenresOrder();

    void setGenresOrder(Order order);

    Observable<Order> getGenresOrderObservable();

    int getSkipConstraintMillis();

    boolean isCoversEnabled();

    boolean isCoversInNotificationEnabled();

    boolean isColoredNotificationEnabled();

    boolean isNotificationCoverStubEnabled();

    boolean isCoversOnLockScreenEnabled();

    boolean isDecreaseVolumeOnAudioFocusLossEnabled();

    void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled);

    boolean isPauseOnAudioFocusLossEnabled();

    void setPauseOnAudioFocusLossEnabled(boolean enabled);

    void setExternalPlayerRepeatMode(int mode);

    int getExternalPlayerRepeatMode();

    Observable<Integer> getExternalPlayerRepeatModeObservable();

    void setExternalPlayerKeepInBackground(boolean enabled);

    boolean isExternalPlayerKeepInBackground();

    void setSelectedEqualizerType(int type);

    int getSelectedEqualizerType();

    Observable<Integer> getSelectedEqualizerTypeObservable();

    Observable<Boolean> getAppConfirmDeleteDialogEnabledObservable();

    void setAppConfirmDeleteDialogEnabled(boolean enabled);

    boolean isAppConfirmDeleteDialogEnabled();

    long getRewindValueMillis();

    void setSleepTimerTime(long millis);

    long getSleepTimerTime();

    void setSleepTimerPlayLastSong(boolean playLastSong);

    boolean isSleepTimerPlayLastSong();

    Observable<Long> getAudioFileMinDurationMillisObservable();

    void setAudioFileMinDurationMillis(long millis);

    long getAudioFileMinDurationMillis();

    void setPauseOnZeroVolumeLevelEnabled(boolean enabled);

    boolean isPauseOnZeroVolumeLevelEnabled();

    Observable<Boolean> getDisplayFileNameObservable();

    void setDisplayFileName(boolean displayFileName);

    boolean isDisplayFileNameEnabled();

    void setShowAllAudioFilesEnabled(boolean enabled);

    boolean isShowAllAudioFilesEnabled();

    Observable<Boolean> getShowAllAudioFilesEnabledObservable();

}
