package com.github.anrimian.musicplayer.ui.common.format.description;

import android.text.SpannableStringBuilder;

import androidx.annotation.NonNull;

public class DescriptionStringBuilder extends SpannableStringBuilder {

    public DescriptionStringBuilder() {
    }

    public DescriptionStringBuilder(CharSequence text) {
        super(text);
    }

    public DescriptionStringBuilder(CharSequence text, int start, int end) {
        super(text, start, end);
    }

    @NonNull
    @Override
    public SpannableStringBuilder append(CharSequence text) {
        if (length() > 0) {
            super.append("\u00A0");//3rd character not space for line break handling

            super.append("● ");
        }
        return super.append(text);
    }
}
