package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

class ThemesViewHolder extends BaseViewHolder {

    @BindView(R.id.tv_theme_name)
    TextView tvTheme;

    @BindView(R.id.rb_theme)
    RadioButton rbTheme;

    @BindView(R.id.fl_clickable_area)
    View flClickableArea;

    private AppTheme appTheme;

    ThemesViewHolder(LayoutInflater inflater,
                     ViewGroup parent,
                     Callback<AppTheme> onThemeClickListener) {
        super(inflater.inflate(R.layout.item_theme, parent, false));
        ButterKnife.bind(this, itemView);

        flClickableArea.setOnClickListener(v -> onThemeClickListener.call(appTheme));
    }

    void bind(AppTheme appTheme, boolean isSelected) {
        this.appTheme = appTheme;

        String description = getContext().getString(appTheme.getDescriptionId());
        tvTheme.setText(description);
        rbTheme.setChecked(isSelected);

        flClickableArea.setContentDescription(description);
        flClickableArea.setClickable(!isSelected);
    }
}
