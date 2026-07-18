package com.github.anrimian.musicplayer.ui.settings.main

import androidx.lifecycle.SavedStateHandle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.interactors.library.MissingFilesInteractor
import com.github.anrimian.musicplayer.domain.interactors.storage.StorageScannerInteractor
import com.github.anrimian.musicplayer.ui.common.error.parser.ErrorParser
import com.github.anrimian.musicplayer.ui.common.mvvm.SimpleViewModel
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import kotlinx.coroutines.rx3.await

class SettingsViewModel(
    missingFilesInteractor: MissingFilesInteractor,
    private val storageScannerInteractor: StorageScannerInteractor,
    savedStateHandle: SavedStateHandle,
    errorParser: ErrorParser,
): SimpleViewModel<SettingsState>(SettingsState(), savedStateHandle, errorParser) {

    init {
        missingFilesInteractor.getMissingFilesCountFlow().subscribe { count ->
            updateState { copy(missingCompositionsCount = count) }
        }
    }

    fun onRescanStorageButtonClicked() {
        launch {
            storageScannerInteractor.runRescanStorage().await()
            sendMessage(R.string.scanning_completed)
        }
    }

    fun onRescanStorageButtonLongClick() {
        launch {
            storageScannerInteractor.rescanStoragePlaylists().await()
            sendMessage(R.string.playlists_scanning_completed)
        }
    }

    fun onMissingCompositionsCountClicked() {
        sendEffect(SettingsEffect.OpenMissingFilesDialog)
    }

    fun onDisplaySettingsClicked() {
        navigateTo(Screen.DisplaySettings)
    }

    fun onLibrarySettingsClicked() {
        navigateTo(Screen.LibrarySettings)
    }

    fun onPlayerSettingsClicked() {
        navigateTo(Screen.PlayerSettings)
    }

    fun onHeadsetSettingsClicked() {
        navigateTo(Screen.HeadsetSettings)
    }

    fun onThemeSettingsClicked() {
        navigateTo(Screen.ThemeSettings)
    }

}