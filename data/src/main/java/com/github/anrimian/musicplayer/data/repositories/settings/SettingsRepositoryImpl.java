package com.github.anrimian.musicplayer.data.repositories.settings;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.models.composition.order.OrderType.ADD_TIME;

/**
 * Created on 16.04.2018.
 */
public class SettingsRepositoryImpl implements SettingsRepository {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String REPEAT_MODE = "repeat_mode";
    private static final String FOLDER_ORDER = "folder_order";
    private static final String COMPOSITIONS_ORDER = "compositions_order";

    private static final String SHOW_COVERS = "show_covers";
    private static final String SHOW_COVERS_IN_NOTIFICATION = "show_covers_in_notification";
    private static final String COLORED_NOTIFICATION = "colored_notification";
    private static final String SHOW_COVERS_ON_LOCK_SCREEN = "show_covers_on_lock_screen";

    private static final String DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS = "decrease_volume_on_audio_focus_loss";

    private final BehaviorSubject<Integer> repeatModeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> randomModeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> folderOrderSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversNotificationSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> coloredNotificationSubject = BehaviorSubject.create();
    private final BehaviorSubject<Boolean> showCoversOnLockScreenSubject = BehaviorSubject.create();

    private SharedPreferencesHelper preferences;

    public SettingsRepositoryImpl(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    @Override
    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.putBoolean(RANDOM_PLAYING_ENABLED, enabled);
        randomModeSubject.onNext(enabled);
    }

    @Override
    public boolean isRandomPlayingEnabled() {
        return preferences.getBoolean(RANDOM_PLAYING_ENABLED);
    }

    @Override
    public Observable<Boolean> getRandomPlayingObservable() {
        return withDefaultValue(randomModeSubject, this::isRandomPlayingEnabled);
    }

    @Override
    public void setRepeatMode(int mode) {
        preferences.putInt(REPEAT_MODE, mode);
        repeatModeSubject.onNext(mode);
    }

    @Override
    public int getRepeatMode() {
        return preferences.getInt(REPEAT_MODE, RepeatMode.NONE);
    }

    @Override
    public Observable<Integer> getRepeatModeObservable() {
        return withDefaultValue(repeatModeSubject, this::getRepeatMode);
    }

    @Override
    public Observable<Order> getFolderOrderObservable() {
        return withDefaultValue(folderOrderSubject, this::getFolderOrder);
    }

    @Override
    public Observable<Boolean> getCoversEnabledObservable() {
        return withDefaultValue(showCoversSubject, this::isCoversEnabled);
    }

    @Override
    public Observable<Boolean> getCoversInNotificationEnabledObservable() {
        return withDefaultValue(showCoversNotificationSubject, this::isCoversInNotificationEnabled);
    }

    @Override
    public Observable<Boolean> getColoredNotificationEnabledObservable() {
        return withDefaultValue(coloredNotificationSubject, this::isColoredNotificationEnabled);
    }

    @Override
    public Observable<Boolean> getCoversOnLockScreenEnabledObservable() {
        return withDefaultValue(showCoversOnLockScreenSubject, this::isCoversOnLockScreenEnabled);
    }

    @Override
    public void setCoversEnabled(boolean enabled) {
        preferences.putBoolean(SHOW_COVERS, enabled);
        showCoversSubject.onNext(enabled);
    }

    @Override
    public void setCoversInNotificationEnabled(boolean enabled) {
        preferences.putBoolean(SHOW_COVERS_IN_NOTIFICATION, enabled);
        showCoversNotificationSubject.onNext(enabled);
    }

    @Override
    public void setColoredNotificationEnabled(boolean enabled) {
        preferences.putBoolean(COLORED_NOTIFICATION, enabled);
        coloredNotificationSubject.onNext(enabled);
    }

    @Override
    public void setCoversOnLockScreenEnabled(boolean enabled) {
        preferences.putBoolean(SHOW_COVERS_ON_LOCK_SCREEN, enabled);
        showCoversOnLockScreenSubject.onNext(enabled);
    }

    @Override
    public Order getFolderOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(FOLDER_ORDER, ADD_TIME.getId() + 1));
    }

    @Override
    public void setFolderOrder(Order order) {
        preferences.putInt(FOLDER_ORDER, orderToInt(order));
        folderOrderSubject.onNext(order);
    }

    @Override
    public Order getCompositionsOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(COMPOSITIONS_ORDER, ADD_TIME.getId() + 1));
    }

    @Override
    public void setCompositionsOrder(Order order) {
        preferences.putInt(COMPOSITIONS_ORDER, orderToInt(order));
    }

    @Override
    public int getSkipConstraintMillis() {
        return 15000;//just 15 seconds. Reserved setting for future
    }

    @Override
    public boolean isCoversEnabled() {
        return preferences.getBoolean(SHOW_COVERS, true);
    }

    @Override
    public boolean isCoversInNotificationEnabled() {
        return preferences.getBoolean(SHOW_COVERS_IN_NOTIFICATION, true);
    }

    @Override
    public boolean isColoredNotificationEnabled() {
        return preferences.getBoolean(COLORED_NOTIFICATION, true);
    }

    @Override
    public boolean isCoversOnLockScreenEnabled() {
        return preferences.getBoolean(SHOW_COVERS_ON_LOCK_SCREEN, false);
    }

    @Override
    public boolean isDecreaseVolumeOnAudioFocusLossEnabled() {
        return preferences.getBoolean(DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS, true);
    }

    @Override
    public void setDecreaseVolumeOnAudioFocusLossEnabled(boolean enabled) {
        preferences.putBoolean(DECREASE_VOLUME_ON_AUDIO_FOCUS_LOSS, enabled);
    }

    private Order orderFromInt(int order) {
        boolean reversed = false;
        if (order % 2 == 0) {
            reversed = true;
            order--;
        }
        return new Order(OrderType.fromId(order), reversed);
    }

    private int orderToInt(Order order) {
        int id = order.getOrderType().getId();
        if (order.isReversed()) {
            id++;
        }
        return id;
    }
}
