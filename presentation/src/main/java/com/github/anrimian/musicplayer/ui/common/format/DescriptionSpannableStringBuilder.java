package com.github.anrimian.musicplayer.ui.common.format;

import android.content.Context;
import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;

public class DescriptionSpannableStringBuilder extends SpannableStringBuilder {

    private final Context context;

    public DescriptionSpannableStringBuilder(Context context) {
        this.context = context;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text) {
        super(text);
        this.context = context;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text, int start, int end) {
        super(text, start, end);
        this.context = context;
    }

    @NonNull
    @Override
    public SpannableStringBuilder append(CharSequence text) {
        if (length() > 0) {
            super.append(" ● ");//TODO split problem • ●
        }
        return super.append(text);
    }
}
