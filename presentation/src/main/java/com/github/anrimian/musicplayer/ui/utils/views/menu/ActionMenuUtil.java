package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MenuItem;

import com.google.android.material.navigation.NavigationView;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.PublicActionMenuPresenter;

public class ActionMenuUtil {

    public static void setupMenu(ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 NavigationView.OnNavigationItemSelectedListener listener) {
        setupMenu(actionMenuView, menuRes, listener, 0);
    }

    @SuppressLint("RestrictedApi")
    public static void setupMenu(ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 NavigationView.OnNavigationItemSelectedListener listener,
                                 int extraItemsCount) {
        Context context = actionMenuView.getContext();
        PublicActionMenuPresenter actionMenuPresenter = new PublicActionMenuPresenter(context);
//        actionMenuPresenter.setReserveOverflow(false);
//        actionMenuPresenter.setWidthLimit(context.getResources().getDisplayMetrics().widthPixels, true);
//        actionMenuPresenter.setItemLimit(Integer.MAX_VALUE);

        ActionBarPolicy abp = ActionBarPolicy.get(context);
        actionMenuPresenter.setItemLimit(abp.getMaxActionButtons() + extraItemsCount);

        MenuBuilder menuBuilder = new MenuBuilder(context);
        new SupportMenuInflater(context).inflate(menuRes, menuBuilder);
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                return listener.onNavigationItemSelected(item);
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {

            }
        });
        menuBuilder.addMenuPresenter(actionMenuPresenter, context);
        actionMenuView.setPresenter(actionMenuPresenter);
        actionMenuPresenter.updateMenuView(true);
    }
}
