package com.github.anrimian.simplemusicplayer.ui.utils.fragments;

import android.app.Activity;
import android.support.design.widget.CoordinatorLayout;
import android.widget.FrameLayout;

/**
 * Created on 20.10.2017.
 */

public class FragmentCoordinatorDelegate {

    private CoordinatorLayout.Behavior behavior;

    private Activity activity;
    private int containerId;

    public FragmentCoordinatorDelegate(Activity activity, int containerId) {
        this.activity = activity;
        this.containerId = containerId;
    }


    public void onAttach() {
        if (behavior != null) {
            return;
        }

        FrameLayout layout = activity.findViewById(containerId);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();

        behavior = params.getBehavior();
        params.setBehavior(null);
    }

    public void onDetach() {
        if (behavior == null) {
            return;
        }

        FrameLayout layout = activity.findViewById(containerId);
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) layout.getLayoutParams();

        params.setBehavior(behavior);

        layout.setLayoutParams(params);

        behavior = null;
    }
}
