package com.github.anrimian.musicplayer.ui.common.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.DialogSpeedSelectorBinding;
import com.github.anrimian.musicplayer.databinding.PartialDeleteDialogBinding;
import com.github.anrimian.musicplayer.databinding.PartialNumberPickerDialogBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.interactors.settings.LibrarySettingsInteractor;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

public class DialogUtils {

    public static void showConfirmDeleteDialog(Context context,
                                               List<Composition> compositions,
                                               Runnable deleteCallback) {
        String message = compositions.size() == 1?
                context.getString(R.string.delete_composition_template, formatCompositionName(compositions.get(0))):
                context.getString(R.string.delete_template, getDativCompositionsMessage(context, compositions.size()));
        showConfirmDeleteFileDialog(context, message, deleteCallback);
    }

    public static void showConfirmDeleteDialog(Context context,
                                               FolderFileSource folder,
                                               Runnable deleteCallback) {
        int filesCount = folder.getFilesCount();
        String name = folder.getName();
        String message;
        if (filesCount == 0) {
            message = context.getString(R.string.delete_empty_folder, name);
        } else {
            message = context.getString(R.string.delete_folder_template,
                    name,
                    getDativCompositionsMessage(context, filesCount));
        }

        showConfirmDeleteFileDialog(context, message, deleteCallback);
    }


    public static void showConfirmDeleteDialog(Context context,
                                               PlayList playList,
                                               Runnable deleteCallback) {
        String message = context.getString(R.string.delete_playlist_template, playList.getName());
        showConfirmDeleteDialog(context, message, deleteCallback);
    }

    public static void showConfirmDeleteFileDialog(Context context,
                                                   String message,
                                                   Runnable deleteCallback) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            LibrarySettingsInteractor interactor = Components.getAppComponent().librarySettingsInteractor();
            if (!interactor.isAppConfirmDeleteDialogEnabled()) {
                deleteCallback.run();
                return;
            }

            PartialDeleteDialogBinding binding = PartialDeleteDialogBinding.inflate(LayoutInflater.from(context));
            ViewUtils.setChecked(binding.cbDoNotShowDeleteDialog, !interactor.isAppConfirmDeleteDialogEnabled());
            ViewUtils.onCheckChanged(
                    binding.cbDoNotShowDeleteDialog,
                    enabled -> interactor.setAppConfirmDeleteDialogEnabled(!enabled)
            );
            new AlertDialog.Builder(context)
                    .setTitle(R.string.deleting)
                    .setMessage(message)
                    .setView(binding.getRoot())
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteCallback.run())
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                    .show();
        } else {
            showConfirmDeleteDialog(context, message, deleteCallback);
        }
    }

    public static void showConfirmDeleteDialog(Context context,
                                               String message,
                                               Runnable deleteCallback) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.deleting)
                .setMessage(message)
                .setPositiveButton(R.string.delete, (dialog, which) -> deleteCallback.run())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public static void shareComposition(Context context, Composition composition) {
        Components.getAppComponent().sourceRepository()
                .getCompositionUri(composition.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(uri -> shareComposition(context, uri))
                .doOnError(t -> showShareCompositionErrorMessage(t, context))
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    public static void shareComposition(Context context, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_STREAM, fileUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)));
    }

    public static void shareCompositions(Context context, Collection<Composition> compositions) {
        Components.getAppComponent().sourceRepository()
                .getCompositionUris(compositions)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSuccess(uris -> shareCompositions(context, uris))
                .doOnError(t -> showShareCompositionErrorMessage(t, context))
                .ignoreElement()
                .onErrorComplete()
                .subscribe();
    }

    public static void shareCompositions(Context context, ArrayList<Uri> uris) {
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("audio/*");
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        StringBuilder sbTitle = new StringBuilder(context.getString(R.string.share));
        sbTitle.append(" (");
        sbTitle.append(context.getResources().getQuantityString(
                R.plurals.files_count,
                uris.size(),
                uris.size()));
        sbTitle.append(")");

        context.startActivity(Intent.createChooser(intent, sbTitle.toString()));
    }

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
        float[] availableSpeedValues = { 0.25f, 0.5f, 0.75f, 1.00f, 1.25f, 1.5f, 1.75f, 2f, 3f };
        float defaultSpeed = 1f;

        DialogSpeedSelectorBinding viewBinding = DialogSpeedSelectorBinding.inflate(
                LayoutInflater.from(context)
        );
        viewBinding.rangeSlider.setStepSize(1f);
        viewBinding.rangeSlider.setValueFrom(0f);
        viewBinding.rangeSlider.setValueTo(availableSpeedValues.length - 1f);
        int currentIndex = 3;
        for (int i = 0; i < availableSpeedValues.length; i++) {
            float value = availableSpeedValues[i];
            if (value == currentSpeed) {
                currentIndex = i;
                break;
            }
        }
        viewBinding.rangeSlider.addOnChangeListener((slider, value, fromUser) -> {
            viewBinding.btnReset.setEnabled(availableSpeedValues[(int) value] != defaultSpeed);
            if (fromUser) {
                AndroidUtils.playTickVibration(context);
            }
        });
        viewBinding.rangeSlider.setValue(currentIndex);
        CompatUtils.setSliderStyle(viewBinding.rangeSlider);

        for (float value: availableSpeedValues) {
            TextView textView = new TextView(context);
            textView.setText(String.format(Locale.getDefault(), "%.2f", value));
            textView.setTextSize(11f);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextColor(getColorFromAttr(context, android.R.attr.textColorSecondary));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            params.weight = 1f;
            viewBinding.thickValuesContainer.addView(textView, params);
        }

        Dialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.playback_speed)
                .setView(viewBinding.getRoot())
                .create();

        viewBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
        viewBinding.btnApply.setOnClickListener(v -> {
            int index = (int) viewBinding.rangeSlider.getValue();
            onSpeedSelected.call(availableSpeedValues[index]);
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

    private static void showShareCompositionErrorMessage(Throwable throwable, Context context) {
        ErrorCommand errorCommand = Components.getAppComponent()
                .errorParser()
                .parseError(throwable);

        Toast.makeText(context, errorCommand.getMessage(), Toast.LENGTH_LONG).show();
    }

    private static String getDativCompositionsMessage(Context context, int count) {
        return context.getResources().getQuantityString(R.plurals.compositions_count_dativ, count, count);
    }
}
