package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.graphics.ColorUtils
import com.github.anrimian.fsync.models.ProgressInfo
import com.github.anrimian.fsync.models.state.file.FileSyncState
import com.github.anrimian.fsync.models.state.file.FileTaskType
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.domain.models.player.MediaPlayers
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionStringBuilder
import com.github.anrimian.musicplayer.ui.common.progress.ProgressView
import com.github.anrimian.musicplayer.ui.common.text.CenteredImageSpan
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.google.android.material.progressindicator.BaseProgressIndicator
import com.google.android.material.progressindicator.BaseProgressIndicatorSpec
import java.io.File

@StringRes
fun getMediaPlayerName(mediaPlayerId: Int) = when(mediaPlayerId) {
    MediaPlayers.EXO_MEDIA_PLAYER -> R.string.exo_media_player
    else -> R.string.android_media_player
}

fun ProgressView.showFileSyncState(
    fileSyncState: FileSyncState?,
    isFileRemote: Boolean,
    animate: Boolean = true,
) {
    /*//debug view
    val time = 3000L
    val timer = java.util.Timer()
    timer.schedule(object : java.util.TimerTask() {
        override fun run() {
            post {
                clearProgress()
                setIconResource(R.drawable.ic_cloud)
                setVisible(true, true)
                postDelayed({
                    setProgressInfo(ProgressInfo())
                    setIconResource(R.drawable.ic_upload)
                    setVisible(true, true)
                    postDelayed({
                        setProgressInfo(ProgressInfo(5, 10))
                        setIconResource(R.drawable.ic_upload)
                        setVisible(true, true)
                        postDelayed({
                            setVisible(false, true, true, true)
                        }, time)
                    }, time)
                }, time)
            }
        }
    }, 0, time * 4)
    return*/
    if (fileSyncState == null) {
        if (isFileRemote) {
            clearProgress()
            setVisible(true, animate)
            setIconResource(R.drawable.ic_cloud)
        } else {
            setVisible(false, animate, clearIcon = true, clearProgress = true)
        }
    } else {
        setVisible(true, animate)
        setProgressInfo(fileSyncState.getProgress())
        setIconResource(when(fileSyncState.taskType) {
            FileTaskType.UPLOAD -> R.drawable.ic_upload
            FileTaskType.DOWNLOAD -> R.drawable.ic_download
        })
    }
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

fun getRemoteViewPlayerState(isPlaying: Boolean, playerState: PlayerState): Int {
    return if (isPlaying) {
        if (playerState == PlayerState.LOADING) {
            AppConstants.RemoteViewPlayerState.PLAY_LOADING
        } else {
            AppConstants.RemoteViewPlayerState.PLAY
        }
    } else {
        AppConstants.RemoteViewPlayerState.PAUSE
    }
}

@DrawableRes
fun getRemoteViewPlayerStateIcon(playerState: Int): Int {
    return when(playerState) {
        AppConstants.RemoteViewPlayerState.PLAY_LOADING -> R.drawable.ic_pause_loading
        AppConstants.RemoteViewPlayerState.PAUSE -> R.drawable.ic_play
        else -> R.drawable.ic_pause
    }
}

@ColorInt
fun Context.getHighlightColor(): Int {
    return ColorUtils.setAlphaComponent(colorFromAttr(R.attr.colorAccent), 76)
}

@DrawableRes
fun getVolumeIcon(volume: Int): Int {
    return if (volume > 0) R.drawable.ic_volume_up else R.drawable.ic_volume_off
}

fun formatFilePath(parentPath: String, name: String): String {
    return if (parentPath.isEmpty()) {
        name
    } else {
        parentPath + File.separator + name
    }
}

fun <T> formatExpandableTextList(
    context: Context,
    items: List<T>,
    description: String,
    isExpanded: Boolean,
    itemFormatter: (T) -> CharSequence,
    maxDisplayItemsCount: Int = 8,
    collapsedItemsCount: Int = 5,
    @DrawableRes itemPrefixDrawableRes: Int = R.drawable.ic_secondary_text_circle,
    onExpandButtonClick: () -> Unit = {},
): SpannableStringBuilder {
    val sb = SpannableStringBuilder(description)

    val collapseList = items.size > maxDisplayItemsCount && !isExpanded
    val displayItem = if (collapseList) {
        items.subList(0, collapsedItemsCount)
    } else {
        items
    }
    displayItem.forEach { item ->
        sb.append("\n   ")
        val imageSpan = CenteredImageSpan(context, itemPrefixDrawableRes)
        sb.setSpan(imageSpan, sb.length - 2, sb.length - 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        sb.append(itemFormatter(item))
        sb.append(';')
    }
    if (collapseList) {
        sb.append("\n")
        val moreText = context.getString(R.string.see_more, items.size - collapsedItemsCount)
        sb.appendClickable(context, moreText, onExpandButtonClick)
    }
    return sb
}

fun SpannableStringBuilder.appendClickable(context: Context, text: String, onClick: () -> Unit) {
    val startIndex = length
    val endIndex = startIndex + text.length
    append(text)
    setSpan(object : ClickableSpan() {
        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.color = context.attrColor(R.attr.colorAccent)
            ds.isUnderlineText = false
        }
        override fun onClick(widget: View) { onClick() }
    }, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
}


fun formatVolumeAdditionalInfo(
    context: Context,
    volume: Volume,
    @DrawableRes dividerDrawableRes: Int = R.drawable.ic_secondary_text_circle,
): SpannableStringBuilder {
    val sb: SpannableStringBuilder = DescriptionSpannableStringBuilder(
        context,
        volume.path,
        dividerDrawableRes
    )
    sb.append(FormatUtils.formatCompositionsCount(context, volume.compositionsCount))
    return sb
}

fun formatVolumeAdditionalInfoForMediaBrowser(
    context: Context,
    volume: Volume,
): String {
    val sb: SpannableStringBuilder = DescriptionStringBuilder()
    sb.append(volume.path)
    sb.append(FormatUtils.formatCompositionsCount(context, volume.compositionsCount))
    return sb.toString()
}