package com.github.anrimian.musicplayer.infrastructure.service.music

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.support.v4.media.session.MediaSessionCompat
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource
import com.github.anrimian.musicplayer.domain.models.player.PlayerState
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting
import com.github.anrimian.musicplayer.domain.utils.Objects
import com.github.anrimian.musicplayer.domain.utils.functions.Optional
import com.github.anrimian.musicplayer.ui.common.format.getRemoteViewPlayerState
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme
import com.github.anrimian.musicplayer.ui.notifications.MediaNotificationsDisplayer
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable

/**
 * Created on 03.11.2017.
 */
class MusicService : Service() {

    private val serviceDisposable = CompositeDisposable()

    //optimization
    private val serviceState = ServiceState()

    private var playerState: PlayerState? = PlayerState.IDLE
    private var isPlayingState = 0
    private var currentSource: CompositionSource? = null
    private var repeatMode = RepeatMode.NONE
    private var notificationSetting: MusicNotificationSetting? = null
    private var currentAppTheme: AppTheme? = null

    override fun onCreate() {
        super.onCreate()
        Components.getAppComponent().mediaSessionHandler().dispatchServiceCreated()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        if (!Permissions.hasFilePermission(this)) {
            notificationsDisplayer().startForegroundErrorNotification(
                this,
                R.string.no_file_permission
            )
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }
        if (intent.getBooleanExtra(START_FOREGROUND_SIGNAL, false)) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && playerState === PlayerState.IDLE) {
                //should reduce chance of RemoteServiceException
                mediaNotificationsDisplayer().startStubForegroundNotification(this, mediaSession())
            }
            startForeground()
        }
        val requestCode = intent.getIntExtra(REQUEST_CODE, -1)
        if (requestCode != -1) {
            handleNotificationAction(requestCode, intent)
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent) = LocalBinder()

    override fun onDestroy() {
        super.onDestroy()
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed()
        serviceDisposable.dispose()
    }

    fun startForeground() {
        //reduce chance to show first notification without info
        var reloadCover = false
        if (notificationSetting == null) {
            reloadCover = true
            currentSource = playerInteractor().getCurrentSource()
            notificationSetting = musicServiceInteractor().notificationSettings
        }
        mediaNotificationsDisplayer().startForegroundNotification(
            this,
            isPlayingState,
            currentSource,
            mediaSession(),
            repeatMode,
            notificationSetting,
            reloadCover
        )
        subscribeOnServiceState()
    }

    private fun handleNotificationAction(requestCode: Int, intent: Intent) {
        when (requestCode) {
            Constants.Actions.PLAY -> {
                val playDelay = intent.getLongExtra(PLAY_DELAY_MILLIS, 0)
                playerInteractor().play(playDelay)
            }
            Constants.Actions.PAUSE -> playerInteractor().pause()
            Constants.Actions.SKIP_TO_NEXT -> musicServiceInteractor().skipToNext()
            Constants.Actions.SKIP_TO_PREVIOUS -> musicServiceInteractor().skipToPrevious()
            Constants.Actions.CHANGE_REPEAT_MODE -> musicServiceInteractor().changeRepeatMode()
        }
    }

    private fun subscribeOnServiceState() {
        if (serviceDisposable.size() != 0) {
            return
        }
        serviceDisposable.add(
            Observable.combineLatest(
                playerInteractor().getIsPlayingStateObservable(),
                playerInteractor().getPlayerStateObservable(),
                playerInteractor().getCurrentSourceObservable(),
                musicServiceInteractor().repeatModeObservable,
                musicServiceInteractor().notificationSettingObservable,
                Components.getAppComponent().themeController().getAppThemeObservable(),
                serviceState::set
            ).observeOn(AndroidSchedulers.mainThread())
                .subscribe(::onServiceStateReceived))

        serviceDisposable.add(Components.getAppComponent().systemServiceController()
            .getStopForegroundSignal()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { stopForeground(false) })
    }

    private fun onServiceStateReceived(serviceState: ServiceState) {
        val newCompositionSource = serviceState.compositionSource!!.value
        val newPlayerState = serviceState.playerState
        var updateNotification = false
        var updateCover = false
        var stopService = false
        if (playerState !== serviceState.playerState) {
            playerState = serviceState.playerState
        }
        val isPlayingState =
            getRemoteViewPlayerState(serviceState.isPlaying, serviceState.playerState!!)
        if (this.isPlayingState != isPlayingState) {
            this.isPlayingState = isPlayingState
            updateNotification = true
        }
        val isSourceEqual = Objects.equals(newCompositionSource, currentSource)
        val isContentEqual =
            CompositionSourceModelHelper.areSourcesTheSame(currentSource, newCompositionSource)
        if (!isSourceEqual || !isContentEqual) {
            currentSource = newCompositionSource
            updateNotification = true
            updateCover = true
        }
        if (repeatMode != serviceState.repeatMode) {
            repeatMode = serviceState.repeatMode
            updateNotification = true
        }
        if (newCompositionSource == null || newPlayerState === PlayerState.IDLE) {
            stopService = true
        }
        val newSettings = serviceState.settings
        if (newSettings != notificationSetting) {
            if (notificationSetting == null || notificationSetting!!.isShowCovers != newSettings!!.isShowCovers || notificationSetting!!.isColoredNotification != newSettings.isColoredNotification || notificationSetting!!.isShowNotificationCoverStub != newSettings.isShowNotificationCoverStub) {
                updateNotification = true
                updateCover = true
            }
            notificationSetting = newSettings
        }
        if (serviceState.appTheme !== currentAppTheme) {
            currentAppTheme = serviceState.appTheme
            updateNotification = true
        }

        //seekbar values on cover settings change
        if (updateNotification && !stopService) {
            updateForegroundNotification(updateCover)
        }
        if (stopService) {
            mediaNotificationsDisplayer().cancelCoverLoadingForForegroundNotification()
            stopForeground(true)
            stopSelf()
        } else {
            if (!mediaSession().isActive) {
                mediaSession().isActive = true
            }
        }
    }

    private fun updateForegroundNotification(reloadCover: Boolean) {
        mediaNotificationsDisplayer().updateForegroundNotification(
            isPlayingState,
            currentSource,
            mediaSession(),
            repeatMode,
            notificationSetting,
            reloadCover
        )
    }

    private fun mediaSession(): MediaSessionCompat {
        return Components.getAppComponent().mediaSessionHandler().getMediaSession()
    }

    private fun playerInteractor(): PlayerInteractor {
        return Components.getAppComponent().playerInteractor()
    }

    private fun musicServiceInteractor(): MusicServiceInteractor {
        return Components.getAppComponent().musicServiceInteractor()
    }

    private fun mediaNotificationsDisplayer(): MediaNotificationsDisplayer {
        return Components.getAppComponent().mediaNotificationsDisplayer()
    }

    private fun notificationsDisplayer(): NotificationsDisplayer {
        return Components.getAppComponent().notificationsDisplayer()
    }

    private class ServiceState {
        var isPlaying = false
        var playerState: PlayerState? = null
        var compositionSource: Optional<CompositionSource>? = null
        var repeatMode = 0
        var settings: MusicNotificationSetting? = null
        var appTheme: AppTheme? = null

        fun set(
            isPlaying: Boolean,
            playerState: PlayerState,
            compositionSource: Optional<CompositionSource>,
            repeatMode: Int,
            settings: MusicNotificationSetting,
            appTheme: AppTheme
        ): ServiceState {
            this.isPlaying = isPlaying
            this.playerState = playerState
            this.compositionSource = compositionSource
            this.repeatMode = repeatMode
            this.settings = settings
            this.appTheme = appTheme
            return this
        }
    }

    inner class LocalBinder : Binder() {
        fun getService() = this@MusicService
    }

    companion object {
        const val REQUEST_CODE = "request_code"
        const val START_FOREGROUND_SIGNAL = "start_foreground_signal"
        const val PLAY_DELAY_MILLIS = "play_delay"
    }
}