package com.github.anrimian.musicplayer.ui.equalizer;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.controllers.music.players.ExoMediaPlayer;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatDialogFragment;

public class EqualizerChooserDialogFragment extends MvpAppCompatDialogFragment {

    @BindView(R.id.rb_use_system_equalizer)
    RadioButton rbSystemEqualizer;

    @BindView(R.id.rb_disable_equalizer)
    RadioButton rbDisableEqualizer;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_equalizer_chooser, null);

        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.equalizer)
                .setView(view)
                .setNegativeButton(R.string.close, (dialog1, which) -> {})
                .create();
        dialog.show();

        rbSystemEqualizer.setOnClickListener(v -> openSystemEqualizer());
        rbDisableEqualizer.setOnClickListener(v -> disableEqualizer());

        return dialog;
    }

    private void openSystemEqualizer() {
        final int sessionId = ExoMediaPlayer.player1.call();

//        final int secondSessionId = AndroidMediaPlayer.player1.getAudioSessionId();
        final int secondSessionId = 0;
        Toast.makeText(requireContext(), "sessionId: " + sessionId + ", secondSessionId: " + secondSessionId, Toast.LENGTH_LONG).show();
        if (sessionId == AudioEffect.ERROR_BAD_VALUE) {
            Toast.makeText(requireContext(), "No Session Id", Toast.LENGTH_LONG).show();
        } else {
            try {
                final Intent effects = new Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
                effects.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().getPackageName());
                effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
                effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
                startActivityForResult(effects, 0);

                rbDisableEqualizer.setChecked(false);
            } catch (@NonNull final ActivityNotFoundException notFound) {
                //check on dialog start and show message
                Toast.makeText(requireContext(), "There is no equalizer", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void disableEqualizer() {
        final int sessionId = ExoMediaPlayer.player1.call();

        Intent intent = new Intent(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION);
        intent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().getPackageName());
        intent.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, sessionId);
        intent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC);
        requireContext().sendBroadcast(intent);

        rbSystemEqualizer.setChecked(false);
    }

}
