package com.github.anrimian.musicplayer.data.controllers.music.equalizer.external;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.widget.Toast;

import com.github.anrimian.musicplayer.data.controllers.music.equalizer.AppEqualizer;

public class ExternalEqualizer implements AppEqualizer {

    private final Context context;

    public ExternalEqualizer(Context context) {
        this.context = context;
    }

    @Override
    public void attachEqualizer(int audioSessionId) {
        Intent intent = new Intent(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        context.sendBroadcast(intent);
    }

    @Override
    public void detachEqualizer(int audioSessionId) {
        Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.getPackageName());
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        context.sendBroadcast(intent);
    }

    public void launchExternalEqualizerSetup(Activity activity, int audioSessionId) {
        if (audioSessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(activity, "No Session Id", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
            intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, activity.getPackageName());
            intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, audioSessionId);
            intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
            activity.startActivityForResult(intent, 0);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(activity, "Unable to start eq: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static boolean isExternalEqualizerExists(Context context) {
        Intent intent = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        return intent.resolveActivity(context.getPackageManager()) != null;
    }
}
