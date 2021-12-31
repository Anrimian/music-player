package com.github.anrimian.musicplayer.ui.editor.common;

import android.os.Build;

import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.di.app.AppComponent;

public class DeleteErrorHandler extends ErrorHandler {

    public DeleteErrorHandler(FragmentManager fm,
                              Runnable onPermissionGranted,
                              Runnable onPermissionDenied) {
        super(fm, onPermissionGranted, () -> {
            AppComponent appComponent = Components.getAppComponent();
            appComponent.storageFilesDataSource().clearDeleteData();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                    && !appComponent.librarySettingsInteractor().isAppConfirmDeleteDialogEnabled()) {
                return;
            }
            onPermissionDenied.run();
        });
    }
}
