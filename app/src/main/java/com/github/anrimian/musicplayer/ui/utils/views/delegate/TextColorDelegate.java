package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.AttrRes;

import static androidx.core.view.ViewCompat.isLaidOut;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

/**
 * Created on 21.01.2018.
 */

public class TextColorDelegate implements SlideDelegate {

    private final int startTextColor;
    private final int endTextColor;

    private final TextView textView;

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    public TextColorDelegate(TextView textView,
                             @AttrRes int startTextColorAttr,
                             @AttrRes int endTextColorAttr) {
        this.textView = textView;

        Context context = textView.getContext();
        this.startTextColor = getColorFromAttr(context, startTextColorAttr);
        this.endTextColor = getColorFromAttr(context, endTextColorAttr);
    }

    @Override
    public void onSlide(float slideOffset) {
        if (isLaidOut(textView)) {
            moveView(slideOffset);
        } else {
            textView.post(() -> moveView(slideOffset));
        }
    }

    private void moveView(float slideOffset) {
        int resultColor = (int) argbEvaluator.evaluate(slideOffset, startTextColor, endTextColor);
        textView.setTextColor(resultColor);
    }
}
