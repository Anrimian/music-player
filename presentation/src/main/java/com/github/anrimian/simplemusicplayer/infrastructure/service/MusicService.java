package com.github.anrimian.simplemusicplayer.infrastructure.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.business.player.MusicPlayerInteractor;
import com.github.anrimian.simplemusicplayer.infrastructure.service.models.NotificationPlayerInfo;
import com.github.anrimian.simplemusicplayer.ui.main.MainActivity;
import com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsHelper;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;

import static com.github.anrimian.simplemusicplayer.ui.notifications.NotificationsHelper.FOREGROUND_NOTIFICATION_ID;

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
    NotificationsHelper notificationsHelper;

    @Inject
    MusicPlayerInteractor musicPlayerInteractor;

    public MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
    public PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
            .setActions(PlaybackStateCompat.ACTION_PLAY
                    | PlaybackStateCompat.ACTION_STOP
                    | PlaybackStateCompat.ACTION_PAUSE
                    | PlaybackStateCompat.ACTION_PLAY_PAUSE
                    | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                    | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS);

    private AudioManager audioManager;
    private MediaSessionCompat mediaSession;
    private AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener();
    private MediaSessionCallback mediaSessionCallback = new MediaSessionCallback();
    private MusicServiceBinder musicServiceBinder = new MusicServiceBinder(this, mediaSession);
    private CompositeDisposable serviceDisposable = new CompositeDisposable();

    @Override
    public void onCreate() {
        super.onCreate();
        Components.getAppComponent().inject(this);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mediaSession = new MediaSessionCompat(this, getClass().getSimpleName());
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS
                | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mediaSession.setCallback(mediaSessionCallback);

        Intent activityIntent = new Intent(this, MainActivity.class);
        PendingIntent pActivityIntent = PendingIntent.getActivity(this, 0, activityIntent, 0);
        mediaSession.setSessionActivity(pActivityIntent);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null, this, MediaButtonReceiver.class);
        PendingIntent pMediaButtonIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mediaSession.setMediaButtonReceiver(pMediaButtonIntent);

        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        subscribeOnPlayerActions();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int requestCode = intent.getIntExtra(REQUEST_CODE, -1);
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
        MediaButtonReceiver.handleIntent(mediaSession, intent);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return musicServiceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(becomingNoisyReceiver);
        mediaSession.release();
        serviceDisposable.dispose();
    }
/*    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        return null;
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {

    }*/

/* .doOnNext(composition -> {
        MediaMetadataCompat metadata = metadataBuilder
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, composition.getTitle())
                .build();
        mediaSession.setMetadata(metadata);
    })*/

    private void subscribeOnPlayerActions() {
        Observable.combineLatest(musicPlayerInteractor.getPlayerStateObservable(),
                musicPlayerInteractor.getCurrentCompositionObservable(),
                NotificationPlayerInfo::new)
                .subscribe(this::onNotificationInfoChanged);
    }

    private void onNotificationInfoChanged(NotificationPlayerInfo info) {
        notificationsHelper.updateForegroundNotification(info, mediaSession);
        switch (info.getState()) {
            case PLAY: {
                int audioFocusResult = audioManager.requestAudioFocus(
                        audioFocusChangeListener,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN);
                if (audioFocusResult != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                    musicPlayerInteractor.pause();
                    return;
                }

                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PLAYING,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());
                mediaSession.setActive(true);
                startForeground(FOREGROUND_NOTIFICATION_ID, notificationsHelper.getForegroundNotification(info, mediaSession));
                break;
            }
            case STOP: {
                mediaSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
                        PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

                //what we need here?
//                mediaSession.setPlaybackState(
//                        stateBuilder.setState(PlaybackStateCompat.STATE_STOPPED,
//                                PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1).build());

                mediaSession.setActive(false);
                audioManager.abandonAudioFocus(audioFocusChangeListener);
                stopForeground(false);
                break;
            }
        }
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: {
                    mediaSessionCallback.onPlay();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                    mediaSessionCallback.onPause();
                    break;
                }
                default: {
                    mediaSessionCallback.onPause();
                    break;
                }
            }
        }
    }

    private class MediaSessionCallback extends MediaSessionCompat.Callback {

        @Override
        public void onPlay() {
            /*
            MediaMetadataCompat metadata = metadataBuilder
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART,
                            BitmapFactory.decodeResource(getResources(), track.getBitmapResId()));
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.getTitle());
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, track.getArtist());
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.getArtist());
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.getDuration())
                    .build();
            mediaSession.setMetadata(metadata);*/
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
    }

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };
}
