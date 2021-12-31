package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.content.res.ColorStateList;
import android.graphics.drawable.RippleDrawable;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemThemeBinding;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

class ThemesViewHolder extends BaseViewHolder {

    private final ItemThemeBinding viewBinding;

    private AppTheme appTheme;

    ThemesViewHolder(ViewGroup parent,
                     Callback<AppTheme> onThemeClickListener) {
        super(parent, R.layout.item_theme);
        viewBinding = ItemThemeBinding.bind(itemView);

        viewBinding.cardView.setOnClickListener(v -> onThemeClickListener.call(appTheme));
    }

    void bind(AppTheme appTheme, boolean isSelected) {
        this.appTheme = appTheme;

        viewBinding.fakeToolbar.setBackgroundResource(appTheme.getPrimaryColorId());
        viewBinding.fakeBackground.setBackgroundResource(appTheme.getBackgroundColorId());
        viewBinding.fakeFab.setColorFilter(ContextCompat.getColor(getContext(), appTheme.getAccentColorId()));
        ((RippleDrawable) viewBinding.cardView.getForeground())
                .setColor(ColorStateList.valueOf(ContextCompat.getColor(getContext(), appTheme.getRippleColorId())));

        setSelected(isSelected);
    }

    void setSelected(boolean isSelected) {
        viewBinding.rbTheme.setChecked(isSelected);
        viewBinding.cardView.setClickable(!isSelected);
    }

    AppTheme getAppTheme() {
        return appTheme;
    }
}
