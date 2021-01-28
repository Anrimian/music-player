package com.github.anrimian.musicplayer.ui.editor.common;

import androidx.fragment.app.FragmentManager;

import com.github.anrimian.musicplayer.di.Components;

public class DeleteErrorHandler extends ErrorHandler {

    public DeleteErrorHandler(FragmentManager fm,
                              Runnable onPermissionGranted,
                              Runnable onPermissionDenied) {
        super(fm, onPermissionGranted, () -> {
            Components.getAppComponent().storageFilesDataSource().clearDeleteData();
            onPermissionDenied.run();
        });
    }
}
