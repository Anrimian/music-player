package com.github.anrimian.musicplayer.ui.settings.themes.view;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;

import butterknife.BindView;
import butterknife.ButterKnife;

class ThemesViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_theme_name)
    TextView tvTheme;

    private AppTheme appTheme;

    ThemesViewHolder(LayoutInflater inflater,
                     ViewGroup parent,
                     Callback<AppTheme> onThemeClickListener) {
        super(inflater.inflate(R.layout.item_theme, parent, false));
        ButterKnife.bind(this, itemView);

        tvTheme.setOnClickListener(v -> onThemeClickListener.call(appTheme));
    }

    void bind(AppTheme appTheme) {
        this.appTheme = appTheme;

        tvTheme.setText(appTheme.getDescriptionId());
    }
}
