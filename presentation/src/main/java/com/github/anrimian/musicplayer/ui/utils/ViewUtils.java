package com.github.anrimian.musicplayer.ui.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import static android.view.View.VISIBLE;
import static androidx.core.view.ViewCompat.isLaidOut;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class ViewUtils {

    public static void run(View view, Runnable runnable) {
        if (isLaidOut(view)) {
            runnable.run();
        } else {
            view.post(runnable);
        }
    }

    public static void animateVisibility(View view, int visibility) {
        animateVisibility(view, visibility, null);
    }

    public static void animateVisibility(View view, int visibility, Runnable onAnimFinished) {
        if (view.getVisibility() != visibility || view.hasTransientState()) {
            Animator animator = getVisibilityAnimator(view, visibility);
            animator.setDuration(visibility == VISIBLE ? 150: 120);
            animator.setInterpolator(visibility == VISIBLE ? new DecelerateInterpolator(): new AccelerateInterpolator());
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    view.clearAnimation();
                    view.setVisibility(visibility);
                    if (onAnimFinished != null) {
                        onAnimFinished.run();
                    }
                }
            });
            animator.start();
        } else {
            if (onAnimFinished != null) {
                onAnimFinished.run();
            }
        }
    }

    public static Animator getVisibilityAnimator(View view, int visibility) {
        float currentVisibility = view.getVisibility() == VISIBLE? 1f: 0f;
        float targetVisibility = visibility == VISIBLE ? 1f : 0f;
        ValueAnimator animator = ObjectAnimator.ofFloat(view, "alpha", currentVisibility, targetVisibility);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (visibility == VISIBLE) {
                    view.setAlpha(currentVisibility);
                    view.setVisibility(VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                view.clearAnimation();
                if (visibility != VISIBLE) {
                    view.setVisibility(visibility);
                }
            }
        });
        return animator;
    }

    public static void animateColor(@ColorInt int from,
                                    @ColorInt int to,
                                    Callback<Integer> onAnimate) {
        ValueAnimator animator = ValueAnimator.ofArgb(from, to);
        animator.setDuration(150);
        animator.addUpdateListener(animation ->
                onAnimate.call((Integer) animation.getAnimatedValue())
        );
        animator.start();
    }

    public static Animator getBackgroundAnimatorAttr(View view,
                                                 @AttrRes int from,
                                                 @AttrRes int to) {
        ValueAnimator animator = getAttrColorAnimator(view.getContext(), from, to);
        animator.addUpdateListener(animation ->
                view.setBackgroundColor((Integer) animation.getAnimatedValue())
        );
        return animator;
    }

    public static Animator getBackgroundAnimator(View view,
                                                 @ColorInt int from,
                                                 @ColorInt int to) {
        ValueAnimator animator = ValueAnimator.ofArgb(from, to);
        animator.addUpdateListener(animation ->
                view.setBackgroundColor((Integer) animation.getAnimatedValue())
        );
        return animator;
    }


    public static ValueAnimator getColorAnimator(@ColorInt int from,
                                                 @ColorInt int to,
                                                 Callback<Integer> onAnimate) {
        ValueAnimator animator = ValueAnimator.ofArgb(from, to);
        animator.addUpdateListener(animation ->
                onAnimate.call((Integer) animation.getAnimatedValue())
        );
        return animator;
    }

    public static ValueAnimator getAttrColorAnimator(Context context,
                                                     @AttrRes int from,
                                                     @AttrRes int to,
                                                     Callback<Integer> onAnimate) {
        ValueAnimator animator = getAttrColorAnimator(context, from, to);
        animator.addUpdateListener(animation ->
                onAnimate.call((Integer) animation.getAnimatedValue())
        );
        return animator;
    }

    public static ValueAnimator getAttrColorAnimator(Context context,
                                                     @AttrRes int from,
                                                     @AttrRes int to) {
        int fromColor = getColorFromAttr(context, from);
        int toColor = getColorFromAttr(context, to);
        return ValueAnimator.ofArgb(fromColor, toColor);
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
