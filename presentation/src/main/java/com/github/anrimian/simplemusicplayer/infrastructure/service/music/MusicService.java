package com.github.anrimian.simplemusicplayer.infrastructure.service.music;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.view.KeyEvent;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.models.PlayerInfo;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.models.TrackInfo;
import com.github.anrimian.simplemusicplayer.infrastructure.service.music.models.mappers.PlayerStateMapper;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsDisplayer;

import javax.annotation.Nonnull;
import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS;
import static android.support.v4.media.session.MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_PLAY_PAUSE;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS;
import static android.support.v4.media.session.PlaybackStateCompat.ACTION_STOP;
import static android.support.v4.media.session.PlaybackStateCompat.Builder;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING;
import static android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED;
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

    private MediaSessionCompat mediaSession;
    private MediaSessionCallback mediaSessionCallback = new MediaSessionCallback();
    private CompositeDisposable serviceDisposable = new CompositeDisposable();

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
        subscribeOnPlayerChanges();
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
                startForeground(FOREGROUND_NOTIFICATION_ID, notificationsDisplayer.getStubNotification());
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
        Observable<Integer> playerStateObservable = musicPlayerInteractor.getPlayerStateObservable()
                .map(PlayerStateMapper::toMediaState);
        Observable<Composition> compositionObservable = musicPlayerInteractor.getCurrentCompositionObservable()
                .map(CurrentComposition::getComposition)
                .doOnNext(this::onCurrentCompositionChanged);
        Observable<Long> trackPositionObservable = musicPlayerInteractor.getTrackPositionObservable();

        serviceDisposable.add(Observable.combineLatest(playerStateObservable, trackPositionObservable, TrackInfo::new)
                .subscribe(this::onTrackInfoChanged));

        serviceDisposable.add(Observable.combineLatest(playerStateObservable, compositionObservable, PlayerInfo::new)
                .subscribe(this::onPlayerStateChanged));
    }

    //TODO can be invalid position in new track(new track emits first), fix later. Maybe don't emit position so often, save on pause/stop?
    private void onTrackInfoChanged(TrackInfo info) {
        stateBuilder.setState(info.getState(), info.getTrackPosition(), 1);
        mediaSession.setPlaybackState(stateBuilder.build());//TODO maybe state can set only in active session?
    }

    private void onCurrentCompositionChanged(Composition composition) {
        MediaMetadataCompat metadata = metadataBuilder
//                .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
//                        BitmapFactory.decodeResource(getResources(), track.getBitmapResId()))
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, composition.getTitle())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, composition.getAlbum())
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, formatCompositionAuthor(composition, this).toString())
                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, composition.getDuration())
                .build();
        mediaSession.setMetadata(metadata);
    }

    private void onPlayerStateChanged(PlayerInfo info) {
        switch (info.getState()) {
            case STATE_PLAYING: {
                startForeground(FOREGROUND_NOTIFICATION_ID, notificationsDisplayer.getForegroundNotification(info));
                mediaSession.setActive(true);
                break;
            }
            case STATE_STOPPED:
            case STATE_PAUSED: {
                notificationsDisplayer.updateForegroundNotification(info);
                stopForeground(false);
                mediaSession.setActive(false);
                break;
            }
        }
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
