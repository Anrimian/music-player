package com.github.anrimian.musicplayer.ui.common.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

interface BaseScreen : Parcelable

@Parcelize
data object CloseScreen : BaseScreen