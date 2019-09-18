package com.github.anrimian.musicplayer.ui.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.MenuRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;

import static android.text.TextUtils.isEmpty;

/**
 * Created on 16.02.2017.
 */

public class AndroidUtils {

    public static int dpToPx(int dp, Context ctx) {
        DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int getColorFromAttr(Context ctx, int attributeId) {
        int colorId = getResourceIdFromAttr(ctx, attributeId);
        return ContextCompat.getColor(ctx, colorId);
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

    public static void requestFocusWithKeyboard(EditText editText) {
        editText.requestFocus();
        editText.postDelayed(() -> {
            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
        }, 200);
    }

    public static void showKeyboard(EditText editText) {
        editText.postDelayed(() -> {
            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
            editText.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
        }, 200);
    }

    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @Nullable
    public static View getContentView(@Nullable Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View decorView = window.getDecorView();
                return decorView.findViewById(android.R.id.content);
            }
        }
        return null;
    }

    public static void setStatusBarColor(Window window, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= 21) {
            window.setStatusBarColor(color);
        }
    }

    public static void sendEmail(Context ctx, String email) {
        if (isEmpty(email)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", email, null));
        try {
            ctx.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            //very low possibility, don't translate yet
            Toast.makeText(ctx, "Mail app  not found", Toast.LENGTH_SHORT).show();
        }
    }

    public static void updateTaskManager(Activity activity,
                                         @StringRes int titleResId,
                                         @DrawableRes int iconResId,
                                         @ColorInt int titleColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription taskDescription;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                taskDescription = new ActivityManager.TaskDescription(
                        activity.getString(titleResId),
                        iconResId,
                        titleColor);
            } else {
                taskDescription = new ActivityManager.TaskDescription(
                        activity.getString(titleResId),
                        BitmapFactory.decodeResource(activity.getResources(), iconResId),
                        titleColor);
            }
            activity.setTaskDescription(taskDescription);
        }
    }

    public static void setSoftInputVisible(Window window) {
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    public static void copyText(Context context, String text, String label) {
        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (cm != null) {
            ClipData clipData = ClipData.newPlainText(label, text);
            cm.setPrimaryClip(clipData);
        }
    }

    @SuppressLint("RestrictedApi")
    public static Menu createMenu(Context context, @MenuRes int menuRes) {
        //noinspection ConstantConditions
        PopupMenu p  = new PopupMenu(context, null);
        Menu menu = p.getMenu();
        new SupportMenuInflater(context).inflate(menuRes, menu);
        return menu;
    }

    public static void playShortVibration(Context context) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        if (v == null) {
            return;
        }
        long vibrationTime = 10;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            v.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(vibrationTime, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(vibrationTime);
        }
    }
}
