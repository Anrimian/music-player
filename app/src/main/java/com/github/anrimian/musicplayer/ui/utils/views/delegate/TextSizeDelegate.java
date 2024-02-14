package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.content.res.Resources;
import android.util.TypedValue;
import android.widget.TextView;

import androidx.annotation.DimenRes;

/**
 * Created on 21.01.2018.
 */

public class TextSizeDelegate extends ViewSlideDelegate<TextView> {

    private final float startTextSize;
    private final float targetTextSize;

    public TextSizeDelegate(TextView textView,
                            @DimenRes int startTextSize,
                            @DimenRes int targetTextSize) {
        super(textView);

        Resources resources = textView.getResources();
        this.startTextSize = resources.getDimensionPixelSize(startTextSize);
        this.targetTextSize = resources.getDimensionPixelSize(targetTextSize);
    }

    @Override
    protected void applySlide(TextView view, float slideOffset) {
        float resultTextSize = (startTextSize + ((targetTextSize - startTextSize) * slideOffset));
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, resultTextSize);
        view.invalidate();
    }

}
