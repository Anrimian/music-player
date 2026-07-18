package com.github.anrimian.musicplayer.ui.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat.Action
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import androidx.core.app.NotificationCompat.VISIBILITY_PUBLIC
import androidx.media.app.NotificationCompat
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.composition.source.LibraryCompositionSource
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.infrastructure.service.music.MusicService
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.getRemoteViewPlayerStateIcon
import com.github.anrimian.musicplayer.ui.common.images.CoverImageLoader
import com.github.anrimian.musicplayer.ui.main.MainActivity
import com.github.anrimian.musicplayer.ui.main.external_player.ExternalPlayerActivity
import com.github.anrimian.musicplayer.ui.notifications.builder.AppNotificationBuilder
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.utils.pIntentFlag
import javax.annotation.Nonnull

class MediaNotificationsDisplayerImpl(
    private val context: Context,
    private val notificationBuilder: AppNotificationBuilder,
    private val coverImageLoader: CoverImageLoader
) : MediaNotificationsDisplayer {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var notificationInfoState: NotificationInfoState? = null
    private var currentNotificationBitmap: Bitmap? = null
    private var cancellationRunnable: Runnable? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                FOREGROUND_CHANNEL_ID,
                context.getString(R.string.foreground_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun startStubForegroundNotification(
        service: Service,
        mediaSession: MediaSessionCompat
    ) {
        NotificationUtils.startMediaPlaybackForeground(service, FOREGROUND_NOTIFICATION_ID, getStubNotification(mediaSession))
    }

    override fun getStubNotification(mediaSession: MediaSessionCompat): Notification {
        val style = NotificationCompat.MediaStyle()
        style.setMediaSession(mediaSession.sessionToken)
        
        val intentPlayPause = Intent(context, MusicService::class.java)
        intentPlayPause.putExtra(MusicService.REQUEST_CODE, AppConstants.Actions.PLAY)
        val pIntentPlayPause = PendingIntent.getService(
            context,
            AppConstants.Actions.PLAY,
            intentPlayPause,
            pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )
        val playPauseAction = Action(
            getRemoteViewPlayerStateIcon(AppConstants.RemoteViewPlayerState.PAUSE),
            getString(R.string.play),
            pIntentPlayPause
        )
        
        return Builder(context, FOREGROUND_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText("")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setShowWhen(false)
            .setPriority(PRIORITY_HIGH)
            .setVisibility(VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_SERVICE)
            .addAction(playPauseAction)
            .setStyle(style)
            .build()
    }

    override fun startForegroundNotification(
        service: Service,
        isPlayingState: Int,
        source: CompositionSource?,
        mediaSession: MediaSessionCompat,
        repeatMode: Int,
        randomMode: Boolean,
        notificationSetting: MusicNotificationSetting?,
        reloadCover: Boolean
    ) {
        notificationInfoState = NotificationInfoState(
            isPlayingState,
            source,
            mediaSession,
            repeatMode,
            randomMode,
            notificationSetting
        )

        val notification = getDefaultMusicNotification(
            isPlayingState,
            source,
            mediaSession,
            repeatMode,
            randomMode,
            notificationSetting
        ).build()
        NotificationUtils.startMediaPlaybackForeground(service, FOREGROUND_NOTIFICATION_ID, notification)

        if (reloadCover) {
            showMusicNotificationWithCover(source, notificationSetting)
        }
    }

    override fun updateForegroundNotification(
        isPlayingState: Int,
        source: CompositionSource?,
        mediaSession: MediaSessionCompat,
        repeatMode: Int,
        randomMode: Boolean,
        notificationSetting: MusicNotificationSetting?,
        reloadCover: Boolean
    ) {
        if (!isNotificationVisible(notificationManager, FOREGROUND_NOTIFICATION_ID)) {
            return
        }

        notificationInfoState = NotificationInfoState(
            isPlayingState,
            source,
            mediaSession,
            repeatMode,
            randomMode,
            notificationSetting
        )

        val notification = getDefaultMusicNotification(
            isPlayingState,
            source,
            mediaSession,
            repeatMode,
            randomMode,
            notificationSetting
        ).build()
        safeNotify(notificationManager, FOREGROUND_NOTIFICATION_ID, notification)

        if (reloadCover) {
            showMusicNotificationWithCover(source, notificationSetting)
        }
    }

    override fun cancelCoverLoadingForForegroundNotification() {
        cancellationRunnable?.run()
    }

    private fun showMusicNotificationWithCover(
        source: CompositionSource?,
        notificationSetting: MusicNotificationSetting?
    ) {
        cancelCoverLoadingForForegroundNotification()

        if (source == null) {
            return
        }

        var showCovers = false
        if (notificationSetting != null) {
            showCovers = notificationSetting.isShowCovers
        }
        if (!showCovers) {
            return
        }

        //keep in mind, we cancel and get short update with an old data
        cancellationRunnable = getCompositionSourceCover(
            source,
            { bitmap ->
                var currentBitmap = bitmap
                val infoState = notificationInfoState ?: return@getCompositionSourceCover

                var showNotificationCoverStub = true
                val setting = infoState.notificationSetting
                if (setting != null) {
                    showNotificationCoverStub = setting.isShowNotificationCoverStub
                }
                if (currentBitmap == null && showNotificationCoverStub) {
                    currentBitmap = coverImageLoader.getDefaultNotificationBitmap()
                }

                val builder = getDefaultMusicNotification(
                    infoState.isPlayingState,
                    infoState.source,
                    infoState.mediaSession,
                    infoState.repeatMode,
                    infoState.randomMode,
                    infoState.notificationSetting
                )

                builder.setLargeIcon(currentBitmap)
                currentNotificationBitmap = currentBitmap
                safeNotify(notificationManager, FOREGROUND_NOTIFICATION_ID, builder.build())
            },
            coverImageLoader
        )
    }

    private fun getDefaultMusicNotification(
        isPlayingState: Int,
        source: CompositionSource?,
        mediaSession: MediaSessionCompat,
        repeatMode: Int,
        randomMode: Boolean,
        notificationSetting: MusicNotificationSetting?
    ): Builder {
        val intent: Intent?
        if (source is ExternalCompositionSource) {
            intent = Intent(context, ExternalPlayerActivity::class.java)
            intent.putExtra(AppConstants.Arguments.LAUNCH_PREPARE_ARG, false)
        } else {
            intent = Intent(context, MainActivity::class.java)
            intent.putExtra(AppConstants.Arguments.OPEN_PLAYER_PANEL_ARG, true)
        }
        val pIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        var coloredNotification = false
        var showNotificationCoverStub = true
        var showCovers = false
        if (notificationSetting != null) {
            coloredNotification = notificationSetting.isColoredNotification
            showNotificationCoverStub = notificationSetting.isShowNotificationCoverStub
            showCovers = notificationSetting.isShowCovers
        }

        val builder = notificationBuilder.buildMusicNotification(context)
            .setColorized(coloredNotification)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(pIntent)
            .setShowWhen(false)
            .setPriority(PRIORITY_HIGH)
            .setVisibility(VISIBILITY_PUBLIC)
            .setCategory(Notification.CATEGORY_SERVICE)

        if (source != null) {
            formatCompositionSource(source, builder)
            setActionsToNotification(
                isPlayingState,
                source,
                mediaSession,
                repeatMode,
                randomMode,
                builder
            )
        } else {
            builder.setContentTitle(context.getString(R.string.app_name))
            
            val intentPlayPause = Intent(context, MusicService::class.java)
            intentPlayPause.putExtra(MusicService.REQUEST_CODE, AppConstants.Actions.PLAY)
            val pIntentPlayPause = PendingIntent.getService(
                context,
                AppConstants.Actions.PLAY,
                intentPlayPause,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val playPauseAction = Action(
                getRemoteViewPlayerStateIcon(AppConstants.RemoteViewPlayerState.PAUSE),
                getString(R.string.play),
                pIntentPlayPause
            )
            builder.addAction(playPauseAction)
        }

        if (showCovers) {
            var bitmap = currentNotificationBitmap
            if (!showNotificationCoverStub && bitmap == coverImageLoader.getDefaultNotificationBitmap()) {
                bitmap = null
            }
            if ((bitmap == null || bitmap.isRecycled) && showNotificationCoverStub) {
                bitmap = coverImageLoader.getDefaultNotificationBitmap()
            }
            builder.setLargeIcon(bitmap)
        }

        return builder
    }

    private fun setActionsToNotification(
        isPlayingState: Int,
        @Nonnull source: CompositionSource,
        mediaSession: MediaSessionCompat,
        repeatMode: Int,
        randomMode: Boolean,
        builder: Builder
    ) {
        val requestCode = if (isPlayingState == AppConstants.RemoteViewPlayerState.PAUSE) {
            AppConstants.Actions.PLAY
        } else {
            AppConstants.Actions.PAUSE
        }
        val intentPlayPause = Intent(context, MusicService::class.java)
        intentPlayPause.putExtra(MusicService.REQUEST_CODE, requestCode)
        val pIntentPlayPause = PendingIntent.getService(
            context,
            requestCode,
            intentPlayPause,
            pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
        )

        val playPauseAction = Action(
            getRemoteViewPlayerStateIcon(isPlayingState),
            getString(if (isPlayingState == AppConstants.RemoteViewPlayerState.PAUSE) R.string.play else R.string.pause),
            pIntentPlayPause
        )

        val style = NotificationCompat.MediaStyle()
        style.setMediaSession(mediaSession.sessionToken)

        if (source is LibraryCompositionSource) {
            val intentChangeRandomMode = Intent(context, MusicService::class.java)
            intentChangeRandomMode.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.CHANGE_SHUFFLE_NODE
            )
            val pIntentChangeRandomMode = PendingIntent.getService(
                context,
                AppConstants.Actions.CHANGE_SHUFFLE_NODE,
                intentChangeRandomMode,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val changeRandomModeAction = Action(
                FormatUtils.getRandomModeIcon(randomMode),
                context.getString(R.string.content_description_shuffle),
                pIntentChangeRandomMode
            )

            val intentSkipToPrevious = Intent(context, MusicService::class.java)
            intentSkipToPrevious.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.SKIP_TO_PREVIOUS
            )
            val pIntentSkipToPrevious = PendingIntent.getService(
                context,
                AppConstants.Actions.SKIP_TO_PREVIOUS,
                intentSkipToPrevious,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            val intentSkipToNext = Intent(context, MusicService::class.java)
            intentSkipToNext.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.SKIP_TO_NEXT
            )
            val pIntentSkipToNext = PendingIntent.getService(
                context,
                AppConstants.Actions.SKIP_TO_NEXT,
                intentSkipToNext,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            val intentChangeRepeatMode = Intent(context, MusicService::class.java)
            intentChangeRepeatMode.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.CHANGE_REPEAT_MODE
            )
            val pIntentChangeRepeatMode = PendingIntent.getService(
                context,
                AppConstants.Actions.CHANGE_REPEAT_MODE,
                intentChangeRepeatMode,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val changeRepeatModeAction = Action(
                FormatUtils.getRepeatModeIcon(repeatMode),
                getString(FormatUtils.getRepeatModeText(repeatMode)),
                pIntentChangeRepeatMode
            )

            style.setShowActionsInCompactView(1, 2, 3)

            builder.addAction(changeRandomModeAction)
                .addAction(
                    R.drawable.ic_skip_previous,
                    getString(R.string.previous_track),
                    pIntentSkipToPrevious
                )
                .addAction(playPauseAction)
                .addAction(
                    R.drawable.ic_skip_next,
                    getString(R.string.next_track),
                    pIntentSkipToNext
                )
                .addAction(changeRepeatModeAction)
        }
        if (source is ExternalCompositionSource) {
            val intentChangeRepeatMode = Intent(context, MusicService::class.java)
            intentChangeRepeatMode.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.CHANGE_REPEAT_MODE
            )
            val pIntentChangeRepeatMode = PendingIntent.getService(
                context,
                AppConstants.Actions.CHANGE_REPEAT_MODE,
                intentChangeRepeatMode,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )
            val changeRepeatModeAction = Action(
                FormatUtils.getRepeatModeIcon(repeatMode),
                getString(FormatUtils.getRepeatModeText(repeatMode)),
                pIntentChangeRepeatMode
            )

            val intentRewind = Intent(context, MusicService::class.java)
            intentRewind.putExtra(MusicService.REQUEST_CODE, AppConstants.Actions.REWIND)
            val pIntentRewind = PendingIntent.getService(
                context,
                AppConstants.Actions.REWIND,
                intentRewind,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            val intentFastForward = Intent(context, MusicService::class.java)
            intentFastForward.putExtra(
                MusicService.REQUEST_CODE,
                AppConstants.Actions.FAST_FORWARD
            )
            val pIntentFastForward = PendingIntent.getService(
                context,
                AppConstants.Actions.FAST_FORWARD,
                intentFastForward,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            val intentClose = Intent(context, MusicService::class.java)
            intentClose.putExtra(MusicService.REQUEST_CODE, AppConstants.Actions.CLOSE)
            val pIntentClose = PendingIntent.getService(
                context,
                AppConstants.Actions.CLOSE,
                intentClose,
                pIntentFlag(PendingIntent.FLAG_UPDATE_CURRENT)
            )

            style.setShowActionsInCompactView(1, 2, 3)

            builder.addAction(changeRepeatModeAction)
                .addAction(R.drawable.ic_rewind, getString(R.string.rewind), pIntentRewind)
                .addAction(playPauseAction)
                .addAction(
                    R.drawable.ic_fast_forward,
                    getString(R.string.fast_forward),
                    pIntentFastForward
                )
                .addAction(R.drawable.ic_close, getString(R.string.close), pIntentClose)
        }

        builder.setStyle(style)
    }

    private fun formatCompositionSource(
        @Nonnull source: CompositionSource,
        builder: Builder
    ) {
        if (source is LibraryCompositionSource) {
            val composition = source.composition
            builder.setContentTitle(CompositionHelper.formatCompositionName(composition))
                .setContentText(FormatUtils.formatCompositionAuthor(composition, context))
        }
        if (source is ExternalCompositionSource) {
            builder.setContentTitle(
                CompositionHelper.formatCompositionName(
                    source.title,
                    source.displayName
                )
            ).setContentText(FormatUtils.formatAuthor(source.artist, context))
        }
    }

    private fun isNotificationVisible(
        notificationManager: NotificationManager,
        notificationId: Int
    ): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val notifications = notificationManager.getActiveNotifications()
                for (notification in notifications) {
                    if (notification.id == notificationId) {
                        return true
                    }
                }
                return false
            } catch (_: Exception) {
            } //getActiveNotifications() can throw exception on android 6
        }
        return true
    }


    private fun safeNotify(
        notificationManager: NotificationManager,
        id: Int,
        notification: Notification?
    ) {
        try {
            notificationManager.notify(id, notification)
        } catch (e: RuntimeException) {
            if (AndroidUtils.isDeadSystemException(e)) {
                return
            }
            throw e
        }
    }

    private fun getString(@StringRes resId: Int): String {
        return context.getString(resId)
    }

    private class NotificationInfoState(
        val isPlayingState: Int,
        val source: CompositionSource?,
        val mediaSession: MediaSessionCompat,
        val repeatMode: Int,
        val randomMode: Boolean,
        val notificationSetting: MusicNotificationSetting?
    )

    companion object {
        private const val FOREGROUND_NOTIFICATION_ID = 1
        const val FOREGROUND_CHANNEL_ID: String = "0"

        private fun getCompositionSourceCover(
            source: CompositionSource,
            onCompleted: (Bitmap?) -> Unit,
            coverImageLoader: CoverImageLoader
        ): Runnable {
            if (source is LibraryCompositionSource) {
                val composition = source.composition
                return coverImageLoader.loadNotificationImage(composition, onCompleted)
            }
            if (source is ExternalCompositionSource) {
                return coverImageLoader.loadNotificationImage(source, onCompleted)
            }
            throw IllegalStateException()
        }
    }
}
