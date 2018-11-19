package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.MenuRes;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.PublicActionMenuPresenter;
import android.view.MenuItem;

public class ActionMenuUtil {

    @SuppressLint("RestrictedApi")
    public static void setupMenu(ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 NavigationView.OnNavigationItemSelectedListener listener) {
        Context context = actionMenuView.getContext();
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
