package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.widget.TextView;

import androidx.annotation.AttrRes;

/**
 * Created on 21.01.2018.
 */

public class TextColorDelegate extends ViewSlideDelegate<TextView> {

    private final int startTextColor;
    private final int endTextColor;

    private final ArgbEvaluator argbEvaluator = new ArgbEvaluator();

    public TextColorDelegate(TextView textView,
                             @AttrRes int startTextColorAttr,
                             @AttrRes int endTextColorAttr) {
        super(textView);
        Context context = textView.getContext();
        this.startTextColor = getColorFromAttr(context, startTextColorAttr);
        this.endTextColor = getColorFromAttr(context, endTextColorAttr);
    }

    @Override
    protected void applySlide(TextView view, float slideOffset) {
        int resultColor = (int) argbEvaluator.evaluate(slideOffset, startTextColor, endTextColor);
        view.setTextColor(resultColor);
    }

}
