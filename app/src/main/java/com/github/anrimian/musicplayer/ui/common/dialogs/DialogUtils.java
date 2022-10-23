package com.github.anrimian.musicplayer.ui.common.dialogs;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Window;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogSpeedSelectorBinding;
import com.github.anrimian.musicplayer.databinding.PartialNumberPickerDialogBinding;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class DialogUtils {

    /**
     * Call in onResume()
     */
    public static void setupBottomSheetDialogMaxWidth(BottomSheetDialogFragment fragment) {
        int width = fragment.requireContext().getResources().getDimensionPixelSize(R.dimen.bottom_sheet_width);
        Dialog dialog = fragment.getDialog();
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.setLayout(width > 0 ? width : MATCH_PARENT, MATCH_PARENT);
            }
        }
    }

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

    public static void showNumberPickerDialog(Context context,
                                              int minValue,
                                              int maxValue,
                                              int currentValue,
                                              Callback<Integer> pickCallback) {
        PartialNumberPickerDialogBinding binding = PartialNumberPickerDialogBinding.inflate(
                LayoutInflater.from(context)
        );

        binding.numberPicker.setMinValue(minValue);
        binding.numberPicker.setMaxValue(maxValue);
        binding.numberPicker.setValue(currentValue);

        new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setPositiveButton(
                        android.R.string.ok,
                        (dialog, which) -> pickCallback.call(binding.numberPicker.getValue())
                ).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }
}
