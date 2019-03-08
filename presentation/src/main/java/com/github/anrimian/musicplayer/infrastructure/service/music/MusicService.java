package com.github.anrimian.musicplayer.infrastructure.service.music;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.RatingCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueEvent;
import com.github.anrimian.musicplayer.domain.models.composition.PlayQueueItem;
import com.github.anrimian.musicplayer.domain.models.player.PlayerState;
import com.github.anrimian.musicplayer.domain.models.player.modes.RepeatMode;
import com.github.anrimian.musicplayer.ui.main.MainActivity;
import com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;
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
import static com.github.anrimian.musicplayer.infrastructure.service.music.models.mappers.PlayerStateMapper.toMediaState;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_NOTIFICATION_ID;

/**
 * Created on 03.11.2017.
 */

public class MusicService extends Service/*MediaBrowserServiceCompat*/ {

    public static final String REQUEST_CODE = "request_code";
    public static final int PLAY = 1;
    public static final int PAUSE = 2;
    public static final int SKIP_TO_NEXT = 3;
    public static final int SKIP_TO_PREVIOUS = 4;

    @Inject
    NotificationsDisplayer notificationsDisplayer;

    @Inject
    MusicPlayerInteractor musicPlayerInteractor;

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

    private long trackPosition;

    @Override
    public void onCreate() {
        super.onCreate();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();//maybe also show notification
            return;
        }
        Components.getAppComponent().inject(this);

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

        subscribeOnPlayerChanges();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        if (requestCode != -1) {
            handleNotificationAction(requestCode);
        } else {
            KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession, intent);
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
        switch (keyEvent.getKeyCode()) {
            case KeyEvent.KEYCODE_MEDIA_PLAY: {
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
        serviceDisposable.add(musicPlayerInteractor.getPlayerStateObservable()
                .subscribe(this::onPlayerStateReceived));
    }

    private void onPlayerStateReceived(PlayerState playerState) {
        this.playerState = playerState;
        updateMediaSessionState(playerState, trackPosition);
        switch (playerState) {
            case PLAY: {
                mediaSession.setActive(true);
                startForeground(FOREGROUND_NOTIFICATION_ID,
                        notificationsDisplayer.getForegroundNotification(
                                true,
                                currentItem,
                                mediaSession));
                subscribeOnPlayInfo();
                break;
            }
            case PAUSE: {
                mediaSession.setActive(false);
                notificationsDisplayer.updateForegroundNotification(
                        false,
                        currentItem,
                        mediaSession);
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
        if (playInfoDisposable.size() == 0) {
            playInfoDisposable.add(musicPlayerInteractor.getCurrentCompositionObservable()
                    .subscribe(this::onCurrentCompositionReceived));
            playInfoDisposable.add(musicPlayerInteractor.getTrackPositionObservable()
                    .subscribe(this::onTrackPositionReceived));
        }
    }

    //little td: don't display notification in the stop state and notification isn't visible(delete intent)
    private void onCurrentCompositionReceived(PlayQueueEvent playQueueEvent) {
        PlayQueueItem queueItem = playQueueEvent.getPlayQueueItem();
        if (queueItem == null) {
            mediaSession.setActive(false);
            stopSelf();
            return;
        }
        if (!queueItem.equals(currentItem)) {
            currentItem = queueItem;
            updateMediaSessionMetadata(currentItem.getComposition());
            notificationsDisplayer.updateForegroundNotification(
                    playerState == PlayerState.PLAY,
                    currentItem,
                    mediaSession);
        }
    }

    private void onTrackPositionReceived(long position) {
        this.trackPosition = position;
        updateMediaSessionState(playerState, trackPosition);
    }

    private void updateMediaSessionMetadata(Composition composition) {
        MediaMetadataCompat metadata = metadataBuilder
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, getCompositionImage(composition))
                .putString(METADATA_KEY_TITLE, composition.getTitle())
                .putString(METADATA_KEY_ALBUM, composition.getAlbum())
                .putString(METADATA_KEY_ARTIST, formatCompositionAuthor(composition, this).toString())
                .putLong(METADATA_KEY_DURATION, composition.getDuration())
                .build();
        mediaSession.setMetadata(metadata);
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
            musicPlayerInteractor.play();
        }

        @Override
        public void onPause() {
            musicPlayerInteractor.pause();
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
