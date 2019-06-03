package com.github.anrimian.musicplayer.ui.settings.player;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.github.anrimian.musicplayer.domain.business.settings.PlayerSettingsInteractor;

@InjectViewState
public class PlayerSettingsPresenter extends MvpPresenter<PlayerSettingsView> {

    private final PlayerSettingsInteractor interactor;

    public PlayerSettingsPresenter(PlayerSettingsInteractor interactor) {
        this.interactor = interactor;
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(
                interactor.isDecreaseVolumeOnAudioFocusLossEnabled()
        );
    }

    void onDecreaseVolumeOnAudioFocusLossChecked(boolean checked) {
        getViewState().showDecreaseVolumeOnAudioFocusLossEnabled(checked);
        interactor.setDecreaseVolumeOnAudioFocusLossEnabled(checked);
    }
}
