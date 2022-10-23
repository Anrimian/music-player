package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.filesync.models.state.file.Downloading
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.filesync.models.state.file.Uploading
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.views.progress_bar.ProgressView
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.progressindicator.BaseProgressIndicatorSpec
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

@StringRes
fun getMediaPlayerName(mediaPlayerId: Int) = when(mediaPlayerId) {
    MediaPlayers.EXO_MEDIA_PLAYER -> R.string.exo_media_player
    else -> R.string.android_media_player
}

fun showFileSyncState(
    fileSyncState: FileSyncState?,
    isFileRemote: Boolean,
    progressView: ProgressView,
) {
    when(fileSyncState) {
        is Uploading -> {
            progressView.setProgressInfo(fileSyncState.getProgress())
            progressView.setIconResource(R.drawable.ic_upload)
        }
        is Downloading -> {
            progressView.setProgressInfo(fileSyncState.getProgress())
            progressView.setIconResource(R.drawable.ic_download)
        }
        else -> {
            progressView.clearProgress()
            if (isFileRemote) {
                progressView.setIconResource(R.drawable.ic_cloud)
            } else {
                progressView.clearIcon()
            }
        }
    }
}

fun getFileSyncStateObservable(id: Long): Observable<FileSyncState> {
    return Components.getAppComponent()
        .syncInteractor()
        .getFileSyncStateObservable(id)
        .observeOn(AndroidSchedulers.mainThread())
}

fun ProgressBar.setProgressInfo(progressInfo: ProgressInfo) {
    val progress = progressInfo.asInt()
    if (progress < 0) {
        isIndeterminate = true
    } else {
        isIndeterminate = false
        AndroidUtils.setProgress(this, progress)
    }
}

fun <S: BaseProgressIndicatorSpec> BaseProgressIndicator<S>.setProgressInfo(progressInfo: ProgressInfo) {
    setExtProgress(progressInfo.asInt())
}

fun <S: BaseProgressIndicatorSpec> BaseProgressIndicator<S>.setExtProgress(progress: Int) {
    if (progress < 0) {
        indeterminate(true)
    } else {
        setProgressCompat(progress, true)
    }
}

fun <S: BaseProgressIndicatorSpec> BaseProgressIndicator<S>.indeterminate(isIndeterminate: Boolean) {
    if (this.isIndeterminate == isIndeterminate) {
        return
    }

    val visible = visibility == View.VISIBLE
    if (visible) {
        visibility = View.INVISIBLE
    }
    this.isIndeterminate = isIndeterminate
    if (visible) {
        visibility = View.VISIBLE
    }
}

private fun ProgressView.setProgressInfo(progressInfo: ProgressInfo) {
    val progress = progressInfo.asInt()
    if (progress < 0) {
        setIndeterminate(true)
    } else {
        setProgress(progress)
    }
}

fun ProgressInfo.asInt(): Int {
    if (total <= 0) {
        return -1
    }
    return ((current.toFloat() / total) * 100).toInt()
}

fun getRemoteViewPlayerState(isPlaying: Boolean, playerState: PlayerState): Int {
    return if (isPlaying) {
        if (playerState == PlayerState.LOADING) {
            Constants.RemoteViewPlayerState.PLAY_LOADING
        } else {
            Constants.RemoteViewPlayerState.PLAY
        }
    } else {
        Constants.RemoteViewPlayerState.PAUSE
    }
}

@DrawableRes
fun getRemoteViewPlayerStateIcon(playerState: Int): Int {
    return when(playerState) {
        Constants.RemoteViewPlayerState.PLAY_LOADING -> R.drawable.ic_pause_loading
        Constants.RemoteViewPlayerState.PAUSE -> R.drawable.ic_play
        else -> R.drawable.ic_pause
    }
}

@ColorInt
fun Context.getHighlightColor(): Int {
    return ColorUtils.setAlphaComponent(colorFromAttr(R.attr.colorAccent), 76)
}
