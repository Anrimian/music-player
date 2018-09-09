package com.github.anrimian.musicplayer.ui.utils.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.github.anrimian.musicplayer.R;

public class FragmentUtils {

    public static void startFragment(Fragment fragment,
                                      FragmentManager fragmentManager,
                                      int container) {
        Fragment existFragment = fragmentManager.findFragmentById(container);
        if (existFragment == null || existFragment.getClass() != fragment.getClass()) {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                    .replace(container, fragment)
                    .commit();
        }
    }
}
