package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;

import java.util.ArrayList;

@SuppressLint("RestrictedApi")
public class SimpleMenuBuilder {

    private final MenuBuilder menuBuilder;

    public SimpleMenuBuilder(Context context) {
        this.menuBuilder = new MenuBuilder(context);
    }

    public void add(int id, String title) {
        menuBuilder.add(0, id, 0, title);
    }

    public ArrayList<MenuItemImpl> getItems() {
        return menuBuilder.getVisibleItems();
    }
}
