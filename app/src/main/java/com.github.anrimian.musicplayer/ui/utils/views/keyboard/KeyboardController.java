package com.github.anrimian.musicplayer.ui.utils.views.keyboard;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

public class KeyboardController {

    private static final String KEYBOARD_FRAGMENT_TAG = "keyboard_fragment";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Activity activity;
    private final KeyboardStateFragment fragment;

    public KeyboardController(AppCompatActivity activity) {
        this.activity = activity;

        FragmentManager fm = activity.getSupportFragmentManager();
        KeyboardStateFragment fragment = (KeyboardStateFragment) fm.findFragmentByTag(KEYBOARD_FRAGMENT_TAG);
        if (fragment == null) {
            fragment = new KeyboardStateFragment();
            fm.beginTransaction()
                    .add(fragment, KEYBOARD_FRAGMENT_TAG)
                    .commit();
        }
        this.fragment = fragment;
    }

    public void dispatchOnPause() {
        boolean isKeyboardWasShown = AndroidUtils.isKeyboardWasShown(getActivityView());
        fragment.setKeyboardWasShown(isKeyboardWasShown);
    }

    public void dispatchOnStart() {
        if (fragment.isKeyboardWasShown()) {
            handler.postDelayed(() -> AndroidUtils.showKeyboard(activity), 100);
        }
    }

    public void hideKeyboard() {
        AndroidUtils.hideKeyboard(getActivityView());
    }

    public void showKeyboard() {
        AndroidUtils.showKeyboard(activity);
    }

    private View getActivityView() {
        return activity.getWindow().getDecorView();
    }
}
