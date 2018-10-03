package com.github.anrimian.musicplayer.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.DimenRes;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created on 16.02.2017.
 */

@SuppressWarnings("WeakerAccess")
public class AndroidUtils {

    public static int getColorFromAttr(Context ctx, int attributeId) {
        int colorId = getResourceIdFromAttr(ctx, attributeId);
        return ContextCompat.getColor(ctx, colorId);
    }

    public static int dpToPx(int dp, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static Drawable getDrawableFromAttr(Context ctx, int attributeId) {
        int drawableId = getResourceIdFromAttr(ctx, attributeId);
        return ContextCompat.getDrawable(ctx, drawableId);
    }

    public static int getResourceIdFromAttr(Context ctx, int attributeId) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = ctx.getTheme();
        theme.resolveAttribute(attributeId, typedValue, true);
        return typedValue.resourceId;
    }

    public static float getFloat(Resources resources, @DimenRes int resId) {
        TypedValue typedValue = new TypedValue();
        resources.getValue(resId, typedValue, true);
        return typedValue.getFloat();
    }

    public static boolean isKeyboardWasShown(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            return imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        return false;
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    public static void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) view.getContext()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
