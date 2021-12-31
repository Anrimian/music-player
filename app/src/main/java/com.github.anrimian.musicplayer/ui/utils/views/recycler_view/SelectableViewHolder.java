package com.github.anrimian.musicplayer.ui.utils.views.recycler_view;

import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;

import static androidx.core.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public abstract class SelectableViewHolder extends BaseViewHolder {
    public SelectableViewHolder(@NonNull ViewGroup parent, int layoutResId) {
        super(parent, layoutResId);
    }

    public abstract void setSelected(boolean selected);

    @ColorInt
    protected int getSelectionColor() {
        return setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorAccent), 25);
    }
}
