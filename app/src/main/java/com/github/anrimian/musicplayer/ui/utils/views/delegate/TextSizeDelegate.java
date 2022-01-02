package com.github.anrimian.musicplayer.ui.utils.views.delegate;

import android.content.res.Resources;
import androidx.annotation.DimenRes;
import android.util.TypedValue;
import android.widget.TextView;

import static androidx.core.view.ViewCompat.isLaidOut;

/**
 * Created on 21.01.2018.
 */

public class TextSizeDelegate implements SlideDelegate {

    private final float startTextSize;
    private final float targetTextSize;

    private final TextView textView;

    public TextSizeDelegate(TextView textView,
                            @DimenRes int startTextSize,
                            @DimenRes int targetTextSize) {
        this.textView = textView;

        Resources resources = textView.getResources();
        this.startTextSize = resources.getDimensionPixelSize(startTextSize);
        this.targetTextSize = resources.getDimensionPixelSize(targetTextSize);
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
        float resultTextSize = (startTextSize + ((targetTextSize - startTextSize) * slideOffset));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resultTextSize);
        textView.invalidate();
    }
}
