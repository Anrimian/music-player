package com.github.anrimian.musicplayer.ui.editor.common

import android.os.Build
import androidx.activity.result.ActivityResultCaller
import com.github.anrimian.musicplayer.di.Components

class DeleteErrorHandler(
    activityResultCaller: ActivityResultCaller,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) : ErrorHandler(activityResultCaller, onPermissionGranted, {
    val appComponent = Components.getAppComponent()
    appComponent.storageFilesDataSource().clearDeleteData()
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R
        || appComponent.librarySettingsInteractor().isAppConfirmDeleteDialogEnabled()) {
        onPermissionDenied()
    }
})