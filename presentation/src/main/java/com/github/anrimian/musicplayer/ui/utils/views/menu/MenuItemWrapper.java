package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.view.MenuItem;

import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import java.util.LinkedList;

import javax.annotation.Nullable;

public class MenuItemWrapper {

    @Nullable
    private MenuItem menuItem;

    private LinkedList<Callback<MenuItem>> deferredFunctions = new LinkedList<>();

    public void setMenuItem(@Nullable MenuItem menuItem) {
        this.menuItem = menuItem;
        while (!deferredFunctions.isEmpty()) {
            deferredFunctions.pollFirst().call(menuItem);
        }
    }

    public void call(Callback<MenuItem> function) {
        if (menuItem != null) {
            function.call(menuItem);
        } else {
            deferredFunctions.add(function);
        }
    }
}
