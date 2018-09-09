package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.MenuRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.PublicActionMenuPresenter;
import android.view.MenuItem;

import com.github.anrimian.musicplayer.R;

public class ActionMenuUtil {

    @SuppressLint("RestrictedApi")
    public static void setupMenu(Context context,
                                 ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 NavigationView.OnNavigationItemSelectedListener listener) {
        PublicActionMenuPresenter actionMenuPresenter = new PublicActionMenuPresenter(context);
        actionMenuPresenter.setReserveOverflow(true);
        actionMenuPresenter.setWidthLimit(context.getResources().getDisplayMetrics().widthPixels, true);
        actionMenuPresenter.setItemLimit(Integer.MAX_VALUE);

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
