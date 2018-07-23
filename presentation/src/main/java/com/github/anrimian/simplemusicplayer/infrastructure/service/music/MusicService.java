package com.github.anrimian.simplemusicplayer.infrastructure.service.music;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.util.Log;
import android.view.KeyEvent;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CompositionEvent;
import com.github.anrimian.simplemusicplayer.domain.models.player.PlayerState;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.models.PlayerMetaState;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsDisplayer;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

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
import static com.github.anrimian.simplemusicplayer.infrastructure.service.music.models.mappers.PlayerStateMapper.toMediaState;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsDisplayer.FOREGROUND_NOTIFICATION_ID;

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

    private MediaSessionCompat mediaSession;

    private Disposable currentCompositionDisposable;
    private Composition currentComposition;

    @Override
    public void onCreate() {
        super.onCreate();
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

        startForeground(FOREGROUND_NOTIFICATION_ID, notificationsDisplayer.getStubNotification());

        subscribeOnPlayerChanges();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
        if (requestCode != -1) {
            handleNotificationAction(requestCode);
        } else {
            KeyEvent keyEvent = MediaButtonReceiver.handleIntent(mediaSession, intent);
            if (keyEvent != null) {
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
        serviceDisposable.add(Observable.combineLatest(
                musicPlayerInteractor.getPlayerStateObservable(),
                getCurrentCompositionObservable(),
                musicPlayerInteractor.getTrackPositionObservable(),
                PlayerMetaState::new)
                .distinctUntilChanged((oldState, newState) -> oldState.getState() == newState.getState())
                .subscribe(this::onPlayerStateChanged));
    }

    private void onPlayerStateChanged(PlayerMetaState playerMetaState) {
        Log.d("KEK", "onPlayerStateChanged: " + playerMetaState);
        currentComposition = playerMetaState.getComposition();

        PlayerState playerState = playerMetaState.getState();
        switch (playerState) {
            case PLAY: {//TODO error with often update notification
                if (currentComposition == null) {
                    return;
                }

                mediaSession.setActive(true);
                updateMediaSession(playerMetaState);
                startForeground(FOREGROUND_NOTIFICATION_ID, notificationsDisplayer.getForegroundNotification(playerMetaState));
                subscribeOnCurrentCompositionChanging();
                break;
            }
            case PAUSE: {
                updateMediaSession(playerMetaState);
                mediaSession.setActive(false);
                notificationsDisplayer.updateForegroundNotification(playerMetaState);
                stopForeground(false);
                break;
            }
            case STOP: {
                stop();
                break;
            }
        }
    }

    private void subscribeOnCurrentCompositionChanging() {
        if (currentCompositionDisposable == null) {
            currentCompositionDisposable = getCurrentCompositionObservable()
                    .withLatestFrom(musicPlayerInteractor.getPlayerStateObservable(),
                            PlayerMetaState::new)
                    .subscribe(this::onCurrentCompositionChanged);
            serviceDisposable.add(currentCompositionDisposable);
        }
    }

    private void onCurrentCompositionChanged(PlayerMetaState playerMetaState) {
        Composition composition = playerMetaState.getComposition();
        Log.d("KEK", "onCurrentCompositionChanged: " + composition);
        if (composition == null) {
            Log.d("KEK", "onCurrentCompositionChanged stop service");
            stop();
            return;
        }
        if (!composition.equals(currentComposition)) {
            currentComposition = composition;
            notificationsDisplayer.updateForegroundNotification(playerMetaState);
        }
    }

    private void stop() {
        Log.d("KEK", "stop");
        notificationsDisplayer.removePlayerNotification();
        stopForeground(true);
        mediaSession.setActive(false);
        stopSelf();
    }

    private Observable<CompositionEvent> getCurrentCompositionObservable() {
        return musicPlayerInteractor.getCurrentCompositionObservable();
    }

    private void updateMediaSession(PlayerMetaState playerMetaState) {
        Composition composition = playerMetaState.getComposition();

        MediaMetadataCompat metadata = metadataBuilder
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                        BitmapFactory.decodeResource(getResources(), track.getBitmapResId()))
                .putString(METADATA_KEY_TITLE, composition.getTitle())
                .putString(METADATA_KEY_ALBUM, composition.getAlbum())
                .putString(METADATA_KEY_ARTIST, formatCompositionAuthor(composition, this).toString())
                .putLong(METADATA_KEY_DURATION, composition.getDuration())
                .build();
        mediaSession.setMetadata(metadata);

        stateBuilder.setState(toMediaState(playerMetaState.getState()),
                playerMetaState.getTrackPosition(),
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
    }
}
