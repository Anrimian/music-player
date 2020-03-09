package com.github.anrimian.musicplayer.ui.common.format.description;

import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;

public class DescriptionSpannableStringBuilder extends SpannableStringBuilder {

    private final Context context;
    private final @AttrRes int textColorAttr;

    public DescriptionSpannableStringBuilder(Context context) {
        this.context = context;
        textColorAttr = android.R.attr.textColorSecondary;
    }

    public DescriptionSpannableStringBuilder(Context context, @AttrRes int textColorAttr) {
        this.context = context;
        this.textColorAttr = textColorAttr;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text) {
        super(text);
        this.context = context;
        textColorAttr = android.R.attr.textColorSecondary;
    }

    public DescriptionSpannableStringBuilder(Context context, CharSequence text, int start, int end) {
        super(text, start, end);
        this.context = context;
        textColorAttr = android.R.attr.textColorSecondary;
    }

    @NonNull
    @Override
    public SpannableStringBuilder append(CharSequence text) {
        if (length() > 0) {
//            super.append(" ● ");//just for compare
            super.append("  \u00A0");//3rd character not space for line break handling

            CenteredImageSpan imageSpan = new CenteredImageSpan(context, R.drawable.ic_description_text_circle);
            imageSpan.setColorFilter(AndroidUtils.getColorFromAttr(context, textColorAttr));
            setSpan(imageSpan, length() - 2, length() - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        }
        return super.append(text);
    }
}
