package com.github.anrimian.musicplayer.ui;

import androidx.annotation.MenuRes;
import android.util.SparseIntArray;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.Screens;

public class ScreensMap {

    private static final SparseIntArray screenMenuMap = new SparseIntArray();
    private static final SparseIntArray screenMenuMapReversed = new SparseIntArray();

    static {
        screenMenuMap.put(R.id.menu_library, Screens.LIBRARY);
        screenMenuMap.put(R.id.menu_play_lists, Screens.PLAY_LISTS);

        screenMenuMapReversed.put(Screens.LIBRARY, R.id.menu_library);
        screenMenuMapReversed.put(Screens.PLAY_LISTS, R.id.menu_play_lists);
    }

    public static int getScreenId(@MenuRes int menuId) {
        return screenMenuMap.get(menuId);
    }

    public static int getMenuIdId(int screenId) {
        return screenMenuMapReversed.get(screenId);
    }
}
