package com.github.anrimian.simplemusicplayer.utils.recycler_view;

import android.support.annotation.StringRes;
import android.view.View;

/**
 * Created on 17.12.2017.
 */

public class TransitionElement {

    private View view;
    private String name;

    public TransitionElement(View view, String name) {
        this.view = view;
        this.name = name;
    }

    public TransitionElement(View view, @StringRes int nameResId) {
        this.view = view;
        this.name = view.getContext().getString(nameResId);
    }

    public View getView() {
        return view;
    }

    public String getName() {
        return name;
    }
}
