package com.github.anrimian.musicplayer.di.app

import com.github.anrimian.musicplayer.ui.common.dialogs.missing.actions.MissingFilesActionsBinder
import dagger.Module
import dagger.Provides

@Module
class ActionsModule {

    @Provides
    fun missingFilesActionsBinder() = MissingFilesActionsBinder()

}