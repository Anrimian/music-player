package com.github.anrimian.musicplayer.ui.equalizer.app;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ActivityEqualizerBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.r0adkll.slidr.Slidr;

import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class EqualizerActivity extends AppCompatActivity implements EqualizerView {

    @InjectPresenter
    EqualizerPresenter presenter;

    private ActivityEqualizerBinding viewBinding;

    @ProvidePresenter
    EqualizerPresenter providePresenter() {
        return Components.getAppComponent().equalizerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityEqualizerBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground);

        setSupportActionBar(viewBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.equalizer);
        }

        Slidr.attach(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
