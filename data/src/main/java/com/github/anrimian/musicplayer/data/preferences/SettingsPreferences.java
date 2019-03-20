package com.github.anrimian.musicplayer.data.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.composition.order.OrderType;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;

import static com.github.anrimian.musicplayer.data.utils.rx.RxUtils.withDefaultValue;
import static com.github.anrimian.musicplayer.domain.models.composition.order.OrderType.ADD_TIME;

/**
 * Created on 16.04.2018.
 */
public class SettingsPreferences {

    private static final String PREFERENCES_NAME = "settings_preferences";

    private static final String RANDOM_PLAYING_ENABLED = "random_playing_enabled";
    private static final String REPEAT_MODE = "repeat_mode";
    private static final String FOLDER_ORDER = "folder_order";
    private static final String COMPOSITIONS_ORDER = "compositions_order";

    private final BehaviorSubject<Integer> repeatModeSubject = BehaviorSubject.create();
    private final BehaviorSubject<Order> folderOrderSubject = BehaviorSubject.create();

    private SharedPreferencesHelper preferences;

    public SettingsPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
        this.preferences = new SharedPreferencesHelper(sharedPreferences);
    }

    public void setRandomPlayingEnabled(boolean enabled) {
        preferences.putBoolean(RANDOM_PLAYING_ENABLED, enabled);
    }

    public boolean isRandomPlayingEnabled() {
        return preferences.getBoolean(RANDOM_PLAYING_ENABLED);
    }

    public void setRepeatMode(int mode) {
        preferences.putInt(REPEAT_MODE, mode);
        repeatModeSubject.onNext(mode);
    }

    public int getRepeatMode() {
        return preferences.getInt(REPEAT_MODE, RepeatMode.NONE);
    }

    public Observable<Integer> getRepeatModeObservable() {
        return withDefaultValue(repeatModeSubject, this::getRepeatMode);
    }

    public Observable<Order> getFolderOrderObservable() {
        return withDefaultValue(folderOrderSubject, this::getFolderOrder);
    }

    public Order getFolderOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(FOLDER_ORDER, ADD_TIME.getId() + 1));
    }

    public void setFolderOrder(Order order) {
        preferences.putInt(FOLDER_ORDER, orderToInt(order));
        folderOrderSubject.onNext(order);
    }

    public Order getCompositionsOrder() {
        // + 1 means reversed order
        return orderFromInt(preferences.getInt(COMPOSITIONS_ORDER, ADD_TIME.getId() + 1));
    }

    public void setCompositionsOrder(Order order) {
        preferences.putInt(COMPOSITIONS_ORDER, orderToInt(order));
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
