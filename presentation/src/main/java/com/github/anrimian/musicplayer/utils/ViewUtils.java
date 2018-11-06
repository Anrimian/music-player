package com.github.anrimian.musicplayer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;
import android.view.View;
import android.widget.TextView;

public class ViewUtils {

    public static void showAsMultiline(Snackbar snackbar) {
        View view = snackbar.getView();
        TextView textView = view.findViewById(android.support.design.R.id.snackbar_text);
        textView.setMaxLines(3);
        snackbar.show();
    }

    @SuppressLint("RestrictedApi")
    public static void showWithIcons(PopupMenu popupMenu, View anchor, Context context) {
        MenuPopupHelper menuHelper = new MenuPopupHelper(context,
                (MenuBuilder) popupMenu.getMenu(),
                anchor);
        menuHelper.setForceShowIcon(true);

        menuHelper.show();
    }
}
