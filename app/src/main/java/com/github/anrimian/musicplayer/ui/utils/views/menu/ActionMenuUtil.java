package com.github.anrimian.musicplayer.ui.utils.views.menu;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.view.ActionBarPolicy;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.PublicActionMenuPresenter;

import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow;

public class ActionMenuUtil {

    public static Menu setupMenu(ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 Callback<MenuItem> listener) {
        return setupMenu(actionMenuView, menuRes, listener, 0);
    }

    @SuppressLint("RestrictedApi")
    public static Menu setupMenu(ActionMenuView actionMenuView,
                                 @MenuRes int menuRes,
                                 Callback<MenuItem> listener,
                                 int extraItemsCount) {
        Context context = actionMenuView.getContext();
        PublicActionMenuPresenter actionMenuPresenter = new PublicActionMenuPresenter(context,
                (anchorView, menuItems) -> PopupMenuWindow.showActionBarPopup(anchorView,
                        menuItems,
                        listener)
        );
//        actionMenuPresenter.setReserveOverflow(false);
//        actionMenuPresenter.setWidthLimit(context.getResources().getDisplayMetrics().widthPixels, true);
//        actionMenuPresenter.setItemLimit(Integer.MAX_VALUE);

        ActionBarPolicy abp = ActionBarPolicy.get(context);
        actionMenuPresenter.setItemLimit(abp.getMaxActionButtons() + extraItemsCount);

        MenuBuilder menuBuilder = new MenuBuilder(context);
        new SupportMenuInflater(context).inflate(menuRes, menuBuilder);
        menuBuilder.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(@NonNull MenuBuilder menu, @NonNull MenuItem item) {
                listener.call(item);
                return true;
            }

            @Override
            public void onMenuModeChange(@NonNull MenuBuilder menu) {

            }
        });
        menuBuilder.addMenuPresenter(actionMenuPresenter, context);
        actionMenuView.setPresenter(actionMenuPresenter);
        actionMenuPresenter.updateMenuView(true);
        return menuBuilder;
    }
}
