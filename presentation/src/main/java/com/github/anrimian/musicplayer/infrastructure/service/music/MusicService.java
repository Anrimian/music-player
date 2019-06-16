package com.github.anrimian.musicplayer.infrastructure.service.music;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.business.player.MusicServiceInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.domain.models.player.service.MusicNotificationSetting;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;
import com.github.anrimian.musicplayer.ui.widgets.WidgetUpdater;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.inject.Named;

import io.reactivex.Scheduler;
import io.reactivex.disposables.CompositeDisposable;

import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ALBUM;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_ARTIST;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_DURATION;
import static android.support.v4.media.MediaMetadataCompat.METADATA_KEY_TITLE;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.Builder;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_GROUP;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_INVALID;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_NONE;
import static android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE;
import static android.support.v4.media.session.PlaybackStateCompat.SHUFFLE_MODE_NONE;
import static com.github.anrimian.musicplayer.Constants.Actions.PAUSE;
import static com.github.anrimian.musicplayer.Constants.Actions.PLAY;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_NEXT;
import static com.github.anrimian.musicplayer.Constants.Actions.SKIP_TO_PREVIOUS;
import static com.github.anrimian.musicplayer.di.app.SchedulerModule.UI_SCHEDULER;
import static com.github.anrimian.musicplayer.infrastructure.service.music.models.mappers.PlayerStateMapper.toMediaState;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.ImageFormatUtils.getCompositionImage;
import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

//TODO observe empty notification fixes and remove logs
public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    public static final String REQUEST_CODE = "request_code";

    @Inject
    NotificationsDisplayer notificationsDisplayer;

    @Inject
    MusicPlayerInteractor musicPlayerInteractor;

    @Inject
    MusicServiceInteractor musicServiceInteractor;

    @Named(UI_SCHEDULER)
    @Inject
    Scheduler uiScheduler;

    @Inject
    WidgetUpdater widgetUpdater;

    public MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    public Builder stateBuilder = new Builder()
            .setActions(ACTION_PLAY
                    | ACTION_STOP
                    | ACTION_PAUSE
                    | ACTION_PLAY_PAUSE
                    | ACTION_SKIP_TO_NEXT
                    | ACTION_SKIP_TO_PREVIOUS);

    private final MediaSessionCallback mediaSessionCallback = new MediaSessionCallback();
    private final CompositeDisposable serviceDisposable = new CompositeDisposable();
    private final CompositeDisposable playInfoDisposable = new CompositeDisposable();

    private MediaSessionCompat mediaSession;

    @Nullable
    private PlayerState playerState;

    @Nullable
    private PlayQueueItem currentItem;

    private MusicNotificationSetting notificationSetting;

    private long trackPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("KEK", "onCreate");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();//maybe also show notification
            return;
        }
        Components.getAppComponent().inject(this);
        widgetUpdater.start();

        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        mediaSession.setFlags(FLAG_HANDLES_MEDIA_BUTTONS | FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        mediaSession.setSessionActivity(pActivityIntent);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, this, MediaButtonReceiver.class);
        PendingIntent pMediaButtonIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pMediaButtonIntent);

        serviceDisposable.add(playInfoDisposable);

        subscribeOnNotificationSettings();
        subscribeOnPlayerChanges();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        Log.d("KEK", "onStartCommand, req code:" + requestCode);
        if (requestCode != -1) {
            handleNotificationAction(requestCode);
        } else {
            KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession, intent);
            Log.d("KEK", "onStartCommand, keyEvent action: " + keyEvent);
            if (keyEvent != null && keyEvent.getAction() == KeyEvent.ACTION_UP) {
                handleMediaButtonAction(keyEvent);
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
        serviceDisposable.dispose();
    }

    private void handleMediaButtonAction(@Nonnull KeyEvent keyEvent) {
        Log.d("KEK", "handleMediaButtonAction, key code: " + keyEvent.getKeyCode());
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY: {
                Log.d("KEK", "handleMediaButtonAction: play");
                musicPlayerInteractor.play();
                break;
            }
        }
    }

    private void handleNotificationAction(int requestCode) {
        switch (requestCode) {
            case PLAY: {
                musicPlayerInteractor.play();
                break;
            }
            case PAUSE: {
                musicPlayerInteractor.pause();
                break;
            }
            case SKIP_TO_NEXT: {
                musicPlayerInteractor.skipToNext();
                break;
            }
            case SKIP_TO_PREVIOUS: {
                musicPlayerInteractor.skipToPrevious();
                break;
            }
        }
    }

    private void subscribeOnPlayerChanges() {
        Log.d("KEK", "subscribeOnPlayerChanges");
        serviceDisposable.add(musicPlayerInteractor.getPlayerStateObservable()
                .observeOn(uiScheduler)
                .subscribe(this::onPlayerStateReceived));
    }

    private void onPlayerStateReceived(PlayerState playerState) {
        Log.d("KEK", "onPlayerStateReceived: " + playerState);
        updateMediaSessionState(playerState, trackPosition);

        //ignore first not play state
//        if (this.playerState == null && playerState != PlayerState.PLAY) {
//            return;
//        }

        this.playerState = playerState;
        switch (playerState) {
            case PLAY: {
                mediaSession.setActive(true);
                Log.d("KEK", "startForeground, currentItem: " + currentItem);
                startForeground(FOREGROUND_NOTIFICATION_ID,
                        notificationsDisplayer.getForegroundNotification(
                                true,
                                currentItem,
                                mediaSession,
                                notificationSetting));
                subscribeOnPlayInfo();
                break;
            }
            case PAUSE: {
                mediaSession.setActive(false);
                notificationsDisplayer.updateForegroundNotification(
                        false,
                        currentItem,
                        mediaSession,
                        notificationSetting);
                stopForeground(false);
                break;
            }
            case STOP: {
                mediaSession.setActive(false);
                stopForeground(true);
                stopSelf();
                break;
            }
        }
    }

    private void subscribeOnPlayInfo() {
        Log.d("KEK", "subscribeOnPlayInfo");
        if (playInfoDisposable.size() == 0) {
            Log.d("KEK", "really subscribeOnPlayInfo: ");
            playInfoDisposable.add(musicPlayerInteractor.getCurrentCompositionObservable()
                    .observeOn(uiScheduler)
                    .subscribe(this::onCurrentCompositionReceived));
            playInfoDisposable.add(musicPlayerInteractor.getTrackPositionObservable()
                    .observeOn(uiScheduler)
                    .subscribe(this::onTrackPositionReceived));
        }
    }

    //little td: don't display notification in the stop state and notification isn't visible(delete intent)
    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem queueItem = playQueueEvent.getPlayQueueItem();
        Log.d("KEK", "onCurrentCompositionReceived: " + queueItem);
        if (queueItem == null) {
            mediaSession.setActive(false);
            stopSelf();
            return;
        }
        if (!queueItem.equals(currentItem)) {
            Log.d("KEK", "onCurrentCompositionReceived: set current");
            currentItem = queueItem;
            updateMediaSessionMetadata(currentItem.getComposition(), notificationSetting);
            notificationsDisplayer.updateForegroundNotification(
                    playerState == PlayerState.PLAY,
                    currentItem,
                    mediaSession,
                    notificationSetting);
        }
    }

    private void onTrackPositionReceived(long position) {
        this.trackPosition = position;
        updateMediaSessionState(playerState, trackPosition);
    }

    private void subscribeOnNotificationSettings() {
        musicServiceInteractor.getNotificationSettingObservable()
                .subscribe(this::onNotificationSettingReceived);
    }

    private void onNotificationSettingReceived(MusicNotificationSetting setting) {
        boolean updateNotification = notificationSetting != null;
        notificationSetting = setting;
        if (updateNotification) {
            notificationsDisplayer.updateForegroundNotification(
                    playerState == PlayerState.PLAY,
                    currentItem,
                    mediaSession,
                    notificationSetting);
            if (currentItem != null) {
                updateMediaSessionMetadata(currentItem.getComposition(), notificationSetting);
            }
        }
    }

    private void updateMediaSessionMetadata(Composition composition, MusicNotificationSetting setting) {
        MediaMetadataCompat.Builder builder = metadataBuilder
                .putString(METADATA_KEY_TITLE, composition.getTitle())
                .putString(METADATA_KEY_ALBUM, composition.getAlbum())
                .putString(METADATA_KEY_ARTIST, formatCompositionAuthor(composition, this).toString())
                .putLong(METADATA_KEY_DURATION, composition.getDuration());
        Bitmap bitmap = null;
        if (setting.isCoversOnLockScreen()) {
            bitmap = getCompositionImage(composition);
            if (bitmap == null) {
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_notification_large_icon);//default icon
            }
        }
        builder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap);
        mediaSession.setMetadata(builder.build());
    }

    private void updateMediaSessionState(PlayerState playerState, long trackPosition) {
        stateBuilder.setState(toMediaState(playerState),
                trackPosition,
                1,
                System.currentTimeMillis());
        mediaSession.setPlaybackState(stateBuilder.build());
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            musicPlayerInteractor.playOrPause();
        }

        @Override
        public void onPause() {
            musicPlayerInteractor.playOrPause();
        }

        @Override
        public void onStop() {
            musicPlayerInteractor.stop();
        }

        @Override
        public void onSkipToNext() {
            musicPlayerInteractor.skipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            musicPlayerInteractor.skipToPrevious();
        }

        //next - test it

        @Override
        public void onSeekTo(long pos) {
            musicPlayerInteractor.seekTo(pos);
        }

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
            musicPlayerInteractor.setRepeatMode(appRepeatMode);
        }

        @Override
        public void onSetShuffleMode(int shuffleMode) {
            musicPlayerInteractor.setRandomPlayingEnabled(shuffleMode != SHUFFLE_MODE_NONE);
        }

        //next - not implemented

        @Override
        public void onCommand(String command, Bundle extras, ResultReceiver cb) {
            super.onCommand(command, extras, cb);
        }

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            return super.onMediaButtonEvent(mediaButtonEvent);
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
        public void onFastForward() {
            super.onFastForward();
        }

        @Override
        public void onRewind() {
            super.onRewind();
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
    }
}
