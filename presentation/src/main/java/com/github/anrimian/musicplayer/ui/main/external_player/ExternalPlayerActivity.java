package com.github.anrimian.musicplayer.ui.main.external_player;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.data.models.composition.source.UriCompositionSource;
import com.github.anrimian.musicplayer.di.Components;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class ExternalPlayerActivity extends MvpAppCompatActivity implements ExternalPlayerView {

    @InjectPresenter
    ExternalPlayerPresenter presenter;

    @ProvidePresenter
    ExternalPlayerPresenter providePresenter() {
        Uri uriToPlay = getIntent().getData();
        UriCompositionSource source = new UriCompositionSource(uriToPlay);
        return Components.getExternalPlayerComponent(source).externalPlayerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        finish();
    }
}
