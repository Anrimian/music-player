package com.github.anrimian.musicplayer.di.app.settings;

import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsPresenter;

import dagger.Subcomponent;

@Subcomponent
public interface SettingsComponent {

    DisplaySettingsPresenter displaySettingsPresenter();
}
