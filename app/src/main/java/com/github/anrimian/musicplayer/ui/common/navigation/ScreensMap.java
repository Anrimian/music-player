package com.github.anrimian.musicplayer.ui.common.navigation;

import android.util.SparseIntArray;

import androidx.annotation.MenuRes;

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

    public static int getMenuId(int screenId) {
        return screenMenuMapReversed.get(screenId);
    }
}
