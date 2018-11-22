package com.github.anrimian.musicplayer.ui.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;

import static androidx.core.view.ViewCompat.isLaidOut;

public class ViewUtils {

    public static void run(View view, Runnable runnable) {
        if (isLaidOut(view)) {
            runnable.run();
        } else {
            view.post(runnable);
        }
    }

    public static void animateVisibility(View view, int visibility) {
        if ((view.getAlpha() == 1f || view.getAlpha() == 0f) && view.getVisibility() != visibility) {
            view.animate()
                    .alpha(visibility == View.VISIBLE ? 1f : 0f)
                    .setDuration(visibility == View.VISIBLE ? 150: 120)
                    .setInterpolator(visibility == View.VISIBLE ? new DecelerateInterpolator(): new AccelerateInterpolator())
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            view.clearAnimation();
                            view.setVisibility(visibility);
                        }
                    })
                    .start();
        }
    }

    public static void showAsMultiline(Snackbar snackbar) {
        View view = snackbar.getView();
        TextView textView = view.findViewById(com.google.android.material.R.id.snackbar_text);
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

    /**
     * Moves icons from the PopupMenu's MenuItems' icon fields into the menu title as a Spannable with the icon and title text.
     */
    public static void insertMenuItemIcons(Context context, PopupMenu popupMenu) {
        Menu menu = popupMenu.getMenu();
        if (hasIcon(menu)) {
            for (int i = 0; i < menu.size(); i++) {
                insertMenuItemIcon(context, menu.getItem(i));
            }
        }
    }

    /**
     * @return true if the menu has at least one MenuItem with an icon.
     */
    private static boolean hasIcon(Menu menu) {
        for (int i = 0; i < menu.size(); i++) {
            if (menu.getItem(i).getIcon() != null) return true;
        }
        return false;
    }

    /**
     * Converts the given MenuItem's title into a Spannable containing both its icon and title.
     */
    private static void insertMenuItemIcon(Context context, MenuItem menuItem) {
        Drawable icon = menuItem.getIcon();

        // If there's no icon, we insert a transparent one to keep the title aligned with the items
        // which do have icons.
        if (icon == null) icon = new ColorDrawable(Color.TRANSPARENT);

        int iconSize = context.getResources().getDimensionPixelSize(R.dimen.popup_menu_item_icon_size);
        icon.setBounds(0, 0, iconSize, iconSize);
        ImageSpan imageSpan = new ImageSpan(icon);

        // Add a space placeholder for the icon, before the title.
        SpannableStringBuilder ssb = new SpannableStringBuilder("     " + menuItem.getTitle());

        // Replace the space placeholder with the icon.
        ssb.setSpan(imageSpan, 0, 1, 0);
        menuItem.setTitle(ssb);
        // Set the icon to null just in case, on some weird devices, they've customized Android to display
        // the icon in the menu... we don't want two icons to appear.
        menuItem.setIcon(null);
    }
}
