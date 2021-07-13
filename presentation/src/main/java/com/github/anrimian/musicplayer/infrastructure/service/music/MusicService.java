package com.github.anrimian.musicplayer.infrastructure.service.music;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;

import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.interactors.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.interactors.player.PlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.source.CompositionSource;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.domain.utils.Objects;
import com.github.anrimian.musicplayer.domain.utils.functions.Optional;
import com.github.anrimian.musicplayer.infrastructure.MediaSessionHandler;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.utils.Permissions;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE;
import static com.github.anrimian.musicplayer.Constants.Actions.CHANGE_REPEAT_MODE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service {

    public static final String REQUEST_CODE = "request_code";
    public static final String START_FOREGROUND_SIGNAL = "start_foreground_signal";
    public static final String PLAY_DELAY_MILLIS = "play_delay";

    private final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();

    //optimization
    private final ServiceState serviceState = new ServiceState();

//    private final MediaSessionCallback mediaSessionCallback = new MediaSessionCallback();
    private final CompositeDisposable serviceDisposable = new CompositeDisposable();

//    private MediaSessionCompat mediaSession;

    private PlayerState playerState = PlayerState.IDLE;
    @Nullable
    private CompositionSource currentSource;
    private long trackPosition;
    private float playbackSpeed = 1f;
    private int repeatMode = RepeatMode.NONE;
    private boolean randomMode;
    private MusicNotificationSetting notificationSetting;
    private AppTheme currentAppTheme;

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().mediaSessionHandler().dispatchServiceCreated();

//        if (!Permissions.hasFilePermission(this)) {
//            notificationsDisplayer().startForegroundErrorNotification(this, R.string.no_file_permission);
//            stopForeground(true);
//            stopSelf();
//            //noinspection UnnecessaryReturnStatement
//            return;
//        }

        //reduce chance to show first notification without info
//        currentSource = playerInteractor().getCurrentSource();
//        notificationSetting = musicServiceInteractor().getNotificationSettings();

        //we must start foreground in onCreate, strange ANR otherwise
//        notificationsDisplayer().startForegroundNotification(this,
//                playerState == PlayerState.PLAY,
//                currentSource,
//                mediaSession(),
//                repeatMode,
//                notificationSetting,
//                true);
//        //update state that depends on current item and settings to keep it actual
//        if (currentSource != null) {
//            updateMediaSessionState();
//            updateMediaSessionMetadata();
//            updateMediaSessionAlbumArt();
//        }

//        subscribeOnServiceState();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        if (!Permissions.hasFilePermission(this)) {
            notificationsDisplayer().startForegroundErrorNotification(this, R.string.no_file_permission);
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }
        int startForegroundSignal = intent.getIntExtra(START_FOREGROUND_SIGNAL, -1);
        if (startForegroundSignal != -1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && playerState == PlayerState.IDLE) {
                //should reduce chance of RemoteServiceException
                notificationsDisplayer().startStubForegroundNotification(this, mediaSession());
            }
            startForeground();
        }
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        if (requestCode != -1) {
            handleNotificationAction(requestCode, intent);
        } /*else {
            KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession(), intent);
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handleMediaButtonAction(keyEvent);
            }
        }*/
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
//        return null;
        return new LocalBinder();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Components.getAppComponent().mediaSessionHandler().dispatchServiceDestroyed();
//        mediaSession().setActive(false);
//        mediaSession().release();
        serviceDisposable.dispose();
    }

    public void startForeground() {
        //reduce chance to show first notification without info
        boolean reloadCover = false;
        if (notificationSetting == null) {
            reloadCover = true;
            currentSource = playerInteractor().getCurrentSource();
            notificationSetting = musicServiceInteractor().getNotificationSettings();
        }
        //update state that depends on current item and settings to keep it actual
        if (currentSource != null) {
            updateMediaSessionState();
            updateMediaSessionMetadata();
            updateMediaSessionAlbumArt();
        }

        notificationsDisplayer().startForegroundNotification(this,
                playerState == PlayerState.PLAY,
                currentSource,
                mediaSession(),
                repeatMode,
                notificationSetting,
                reloadCover);

        subscribeOnServiceState();
    }

//    private void handleMediaButtonAction(@Nonnull KeyEvent keyEvent) {
//        /* player interactor not null check because case:
//         * 1) start-stop play
//         * 2) enable bluetooth connection receiver
//         * 3) hide activity
//         * 4) revoke permission
//         * 5) connect bluetooth device
//         * 6) use play button from device
//         * 7) resume activity from task manager
//         *
//         * not actual, but leave, it's interesting memory
//         */
//        Log.d("KEK", "handleMediaButtonAction");
//        if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY) {
//            Log.d("KEK", "play from media button");
//            playerInteractor().play();
//        }
//    }

    private void handleNotificationAction(int requestCode, Intent intent) {
        switch (requestCode) {
            case PLAY: {
                int playDelay = intent.getIntExtra(PLAY_DELAY_MILLIS, 0);
                playerInteractor().play(playDelay);
                break;
            }
            case PAUSE: {
                playerInteractor().pause();
                break;
            }
            case SKIP_TO_NEXT: {
                musicServiceInteractor().skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                musicServiceInteractor().skipToPrevious();
                break;
            }
            case CHANGE_REPEAT_MODE: {
                musicServiceInteractor().changeRepeatMode();
                break;
            }
        }
    }

    private void subscribeOnServiceState() {
        if (serviceDisposable.size() != 0) {
            return;
        }
        serviceDisposable.add(Observable.combineLatest(playerInteractor().getPlayerStateObservable(),
                playerInteractor().getCurrentSourceObservable(),
                playerInteractor().getTrackPositionObservable(),
                playerInteractor().getCurrentPlaybackSpeedObservable(),
                musicServiceInteractor().getRepeatModeObservable(),
                musicServiceInteractor().getRandomModeObservable(),
                musicServiceInteractor().getNotificationSettingObservable(),
                Components.getAppComponent().themeController().getAppThemeObservable(),
                serviceState::set)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::onServiceStateReceived));

    }

    private void onServiceStateReceived(ServiceState serviceState) {
        CompositionSource newCompositionSource = serviceState.compositionSource.getValue();
        PlayerState newPlayerState = serviceState.playerState;
        long newTrackPosition = serviceState.trackPosition;

        boolean updateNotification = false;
        boolean updateMediaSessionState = false;
        boolean updateMediaSessionMetadata = false;
        boolean updateMediaSessionAlbumArt = false;
        boolean stopService = false;

        if (this.playerState != serviceState.playerState) {
            this.playerState = serviceState.playerState;
            updateNotification = true;
            updateMediaSessionState = true;

            if (playerState == PlayerState.PAUSE || playerState == PlayerState.STOP) {
                stopForeground(false);
            }
        }

        boolean isSourceEqual = Objects.equals(newCompositionSource, currentSource);
        boolean isContentEqual = CompositionSourceModelHelper.areSourcesTheSame(currentSource, newCompositionSource);
        if (!isSourceEqual || !isContentEqual) {
            if (!isSourceEqual) {
                newTrackPosition = CompositionSourceModelHelper.getTrackPosition(newCompositionSource);
            }
            this.currentSource = newCompositionSource;
            updateNotification = true;
            updateMediaSessionMetadata = true;
            updateMediaSessionAlbumArt = true;
        }

        if (this.trackPosition != newTrackPosition) {
            this.trackPosition = newTrackPosition;
            updateMediaSessionState = true;
        }

        if (this.repeatMode != serviceState.repeatMode) {
            this.repeatMode = serviceState.repeatMode;
            updateMediaSessionState = true;
            updateNotification = true;
        }

        if (this.randomMode != serviceState.randomMode) {
            this.randomMode = serviceState.randomMode;
            updateMediaSessionState = true;
        }

        if (this.playbackSpeed != serviceState.playbackSpeed) {
            this.playbackSpeed = serviceState.playbackSpeed;
            updateMediaSessionState = true;
        }

        if (newCompositionSource == null || newPlayerState == PlayerState.IDLE) {
            stopService = true;
        }

        MusicNotificationSetting newSettings = serviceState.settings;
        if (!newSettings.equals(this.notificationSetting)) {
            if (notificationSetting == null
                    || notificationSetting.isCoversOnLockScreen() != newSettings.isCoversOnLockScreen()) {
                updateMediaSessionAlbumArt = true;
            }
            if (notificationSetting == null
                    || notificationSetting.isShowCovers() != newSettings.isShowCovers()
                    || notificationSetting.isColoredNotification() != newSettings.isColoredNotification()
                    || notificationSetting.isShowNotificationCoverStub() != newSettings.isShowNotificationCoverStub()) {
                updateNotification = true;
            }
            this.notificationSetting = newSettings;
        }
        if (serviceState.appTheme != currentAppTheme) {
            currentAppTheme = serviceState.appTheme;
            updateNotification = true;
        }

        //seekbar values on cover settings change
        if (updateNotification && !stopService) {
            updateForegroundNotification(updateMediaSessionAlbumArt);
        }
        if (updateMediaSessionState) {
            updateMediaSessionState();
        }
        if (updateMediaSessionMetadata) {
            updateMediaSessionMetadata();
        }
        if (updateMediaSessionAlbumArt) {
            updateMediaSessionAlbumArt();
        }
        if (stopService) {
            notificationsDisplayer().cancelCoverLoadingForForegroundNotification();
            stopForeground(true);
            stopSelf();
        } else {
            if (!mediaSession().isActive()) {
                mediaSession().setActive(true);
            }
        }
    }

    private void updateMediaSessionAlbumArt() {
        CompositionSourceModelHelper.updateMediaSessionAlbumArt(currentSource,
                metadataBuilder,
                mediaSession(),
                notificationSetting.isCoversOnLockScreen());
    }

    private void updateMediaSessionMetadata() {
        CompositionSourceModelHelper.updateMediaSessionMetadata(currentSource,
                metadataBuilder,
                mediaSession(),
                this);
    }

    private void updateMediaSessionState() {
        MediaSessionHandler mediaSessionHandler = Components.getAppComponent()
                .mediaSessionHandler();
        mediaSessionHandler.updatePlaybackState(playerState, trackPosition, playbackSpeed);
        mediaSessionHandler.setRepeatMode(repeatMode);

        int sessionShuffleMode;
        if (randomMode) {
            sessionShuffleMode = SHUFFLE_MODE_ALL;
        } else {
            sessionShuffleMode = SHUFFLE_MODE_NONE;
        }
        mediaSession().setShuffleMode(sessionShuffleMode);
    }

    private void updateForegroundNotification(boolean reloadCover) {
        notificationsDisplayer().updateForegroundNotification(
                playerState == PlayerState.PLAY,
                currentSource,
                mediaSession(),
                repeatMode,
                notificationSetting,
                reloadCover);
    }

    private MediaSessionCompat mediaSession() {
        return Components.getAppComponent().mediaSessionHandler().getMediaSession();

//        if (mediaSession == null) {
//            mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
//            mediaSession.setCallback(mediaSessionCallback);
//
//            Intent activityIntent = new Intent(this, MainActivity.class);
//            PendingIntent pActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
//            mediaSession.setSessionActivity(pActivityIntent);
//
//            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, this, AppMediaButtonReceiver.class);
//            PendingIntent pMediaButtonIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
//            mediaSession.setMediaButtonReceiver(pMediaButtonIntent);
//        }
//        return mediaSession;
    }

    private PlayerInteractor playerInteractor() {
        return Components.getAppComponent().playerInteractor();
    }

    private MusicServiceInteractor musicServiceInteractor() {
        return Components.getAppComponent().musicServiceInteractor();
    }

    private NotificationsDisplayer notificationsDisplayer() {
        return Components.getAppComponent().notificationDisplayer();
    }

    private static class ServiceState {
        PlayerState playerState;
        Optional<CompositionSource> compositionSource;
        long trackPosition;
        float playbackSpeed;
        int repeatMode;
        boolean randomMode;
        MusicNotificationSetting settings;
        AppTheme appTheme;

        private ServiceState set(PlayerState playerState,
                                 Optional<CompositionSource> compositionSource,
                                 long trackPosition,
                                 float playbackSpeed,
                                 int repeatMode,
                                 boolean randomMode,
                                 MusicNotificationSetting settings,
                                 AppTheme appTheme) {
            this.playerState = playerState;
            this.compositionSource = compositionSource;
            this.trackPosition = trackPosition;
            this.playbackSpeed = playbackSpeed;
            this.repeatMode = repeatMode;
            this.randomMode = randomMode;
            this.settings = settings;
            this.appTheme = appTheme;
            return this;
        }
    }

    /*private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            playerInteractor().play();
        }

        @Override
        public void onPause() {
            playerInteractor().pause();
        }

        @Override
        public void onStop() {
            playerInteractor().stop();
        }

        @Override
        public void onSkipToNext() {
            musicServiceInteractor().skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            musicServiceInteractor().skipToPrevious();
        }

        @Override
        public void onSeekTo(long pos) {
            playerInteractor().onSeekFinished(pos);
        }

        //next - test it

        @Override
        public void onSetRepeatMode(int repeatMode) {
            int appRepeatMode;
            switch (repeatMode) {
                case REPEAT_MODE_INVALID:
                case REPEAT_MODE_NONE: {
                    appRepeatMode = RepeatMode.NONE;
                    break;
                }
                case REPEAT_MODE_GROUP:
                case REPEAT_MODE_ALL: {
                    appRepeatMode = RepeatMode.REPEAT_PLAY_LIST;
                    break;
                }
                case REPEAT_MODE_ONE: {
                    appRepeatMode = RepeatMode.REPEAT_COMPOSITION;
                    break;
                }
                default: {
                    appRepeatMode = RepeatMode.NONE;
                }
            }
            musicServiceInteractor().setRepeatMode(appRepeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            musicServiceInteractor().setRandomPlayingEnabled(shuffleMode != SHUFFLE_MODE_NONE);
        }

        @Override
        public void onFastForward() {
            playerInteractor().fastSeekForward();
        }

        @Override
        public void onRewind() {
            playerInteractor().fastSeekBackward();
        }

        @Override
        public void onSetPlaybackSpeed(float speed) {
            musicServiceInteractor().setPlaybackSpeed(speed);
        }

        //next - not implemented

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public void onPrepare() {
            super.onPrepare();
        }

        @Override
        public void onPrepareFromMediaId(String mediaId, Bundle extras) {
            super.onPrepareFromMediaId(mediaId, extras);
        }

        @Override
        public void onPrepareFromSearch(String query, Bundle extras) {
            super.onPrepareFromSearch(query, extras);
        }

        @Override
        public void onPrepareFromUri(Uri uri, Bundle extras) {
            super.onPrepareFromUri(uri, extras);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            super.onPlayFromMediaId(mediaId, extras);
        }

        @Override
        public void onPlayFromSearch(String query, Bundle extras) {
            super.onPlayFromSearch(query, extras);
        }

        @Override
        public void onPlayFromUri(Uri uri, Bundle extras) {
            super.onPlayFromUri(uri, extras);
        }

        @Override
        public void onSkipToQueueItem(long id) {
            super.onSkipToQueueItem(id);
        }

        @Override
        public void onSetRating(RatingCompat rating) {
            super.onSetRating(rating);
        }

        @Override
        public void onSetRating(RatingCompat rating, Bundle extras) {
            super.onSetRating(rating, extras);
        }

        @Override
        public void onSetCaptioningEnabled(boolean enabled) {
            super.onSetCaptioningEnabled(enabled);
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            super.onCustomAction(action, extras);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            super.onAddQueueItem(description);
        }

        @Override
        public void onAddQueueItem(MediaDescriptionCompat description, int index) {
            super.onAddQueueItem(description, index);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            super.onRemoveQueueItem(description);
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
        }
    }*/

    public class LocalBinder extends Binder {

        public MusicService getService() {
            return MusicService.this;
        }
    }

}
