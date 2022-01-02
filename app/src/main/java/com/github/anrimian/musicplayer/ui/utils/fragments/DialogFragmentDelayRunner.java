package com.github.anrimian.musicplayer.ui.utils.fragments;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

@SuppressWarnings("WeakerAccess")
public class DialogFragmentDelayRunner {

    private final Handler handler = new Handler(Looper.getMainLooper());

    private final long delayMillis;
    private final FragmentManager fragmentManager;
    private final String tag;

    public DialogFragmentDelayRunner(FragmentManager fragmentManager, String tag) {
        this(200, fragmentManager, tag);
    }

    public DialogFragmentDelayRunner(long delayMillis,
                                     FragmentManager fragmentManager,
                                     String tag) {
        this.delayMillis = delayMillis;
        this.fragmentManager = fragmentManager;
        this.tag = tag;
    }

    public void show(DialogFragment fragment) {
        handler.postDelayed(() -> {
            if (!fragmentManager.isDestroyed()) {
                try {
                    fragment.show(fragmentManager, tag);
                    //https://issuetracker.google.com/issues/37133130
                } catch (IllegalStateException ignored) {}
            }
        }, delayMillis);
    }

    public void cancel() {
        handler.removeCallbacksAndMessages(null);
        handler.post(() -> {
            DialogFragment fragment = (DialogFragment) fragmentManager
                    .findFragmentByTag(tag);
            if (fragment != null) {
                fragment.dismissAllowingStateLoss();
            }
        });
    }
}
