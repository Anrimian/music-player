package com.github.anrimian.musicplayer.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogSpeedSelectorBinding;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;

public class DialogUtils {

    public static void showSpeedSelectorDialog(Context context,
                                               float currentSpeed,
                                               Callback<Float> onSpeedSelected) {
        float minSpeed = 0.25f;
        float maxSpeed = 2.00f;
        float defaultSpeed = 1f;

        var viewBinding = DialogSpeedSelectorBinding.inflate(LayoutInflater.from(context));

        viewBinding.sbSpeed.setMax((int) ((maxSpeed - minSpeed) * 100));
        SeekBarViewWrapper seekBarViewWrapper = new SeekBarViewWrapper(viewBinding.sbSpeed);
        seekBarViewWrapper.setProgressChangeListener(progress -> {
            float value = (progress / 100f) + minSpeed;
            viewBinding.btnReset.setEnabled(value != defaultSpeed);
            viewBinding.tvCurrentSpeed.setText(context.getString(R.string.playback_speed_template, value));
        });
        seekBarViewWrapper.setProgress((int) ((currentSpeed - minSpeed) * 100));

        viewBinding.tvSpeedMin.setText(context.getString(R.string.playback_speed_template, minSpeed));
        viewBinding.tvSpeedMax.setText(context.getString(R.string.playback_speed_template, maxSpeed));
        viewBinding.tvCurrentSpeed.setText(context.getString(R.string.playback_speed_template, currentSpeed));

        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.playback_speed)
                .setView(viewBinding.getRoot())
                .create();

        viewBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        viewBinding.btnApply.setOnClickListener(v -> {
            float value = (viewBinding.sbSpeed.getProgress() / 100f) + minSpeed;
            onSpeedSelected.call(value);
            dialog.dismiss();
        });
        viewBinding.btnReset.setOnClickListener(v -> {
            onSpeedSelected.call(defaultSpeed);
            dialog.dismiss();
        });

        dialog.show();
    }

}
