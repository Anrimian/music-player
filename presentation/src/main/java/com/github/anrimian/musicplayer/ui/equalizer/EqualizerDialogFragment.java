package com.github.anrimian.musicplayer.ui.equalizer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerType;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer;
import com.github.anrimian.musicplayer.databinding.DialogEqualizerBinding;
import com.github.anrimian.musicplayer.databinding.PartialEqualizerBandBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.domain.utils.ListUtils;
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.LinkedList;
import java.util.List;

import moxy.MvpBottomSheetDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.setupBottomSheetDialogMaxWidth;

public class EqualizerDialogFragment extends MvpBottomSheetDialogFragment
        implements EqualizerView {

    @InjectPresenter
    EqualizerPresenter presenter;

    private DialogEqualizerBinding viewBinding;

    private EqualizerController equalizerController;

    private final List<Pair<PartialEqualizerBandBinding, Band>> bandsViewList = new LinkedList<>();
    private boolean inAppEqualizerSettingsEnabled;

    @ProvidePresenter
    EqualizerPresenter providePresenter() {
        return Components.getAppComponent().equalizerPresenter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        viewBinding = DialogEqualizerBinding.inflate(LayoutInflater.from(getContext()));
        View view = viewBinding.getRoot();
        dialog.setContentView(view);

//        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();
//
//        int height = displayMetrics.heightPixels;
//
//        float heightPercent = getFloat(getResources(), R.dimen.choose_playlist_dialog_height);
//        int minHeight = (int) (height * heightPercent);
//        view.setMinimumHeight(minHeight);

        view.measure(
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        BottomSheetBehavior bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog);
        bottomSheetBehavior.addBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }
        }, o -> {}));
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground);

        equalizerController = Components.getAppComponent().equalizerController();

//        AlertDialog dialog = new AlertDialog.Builder(getActivity())
//                .setTitle(R.string.equalizer)
//                .setView(viewBinding.getRoot())
//                .setNegativeButton(R.string.close, (dialog1, which) -> {})
//                .create();
//        dialog.show();

        viewBinding.rbUseSystemEqualizer.setOnClickListener(v -> enableSystemEqualizer());
        viewBinding.btnOpenSystemEqualizer.setOnClickListener(v -> openSystemEqualizer());
        viewBinding.rbUseAppEqualizer.setOnClickListener(v -> enableAppEqualizer());
//        viewBinding.btnOpenAppEqualizer.setOnClickListener(v -> openAppEqualizer());
        viewBinding.rbDisableEqualizer.setOnClickListener(v -> disableEqualizer());

        showActiveEqualizer(equalizerController.getSelectedEqualizerType());

        CompatUtils.setOutlineButtonStyle(viewBinding.btnOpenSystemEqualizer);
    }

    @Override
    public void onResume() {
        super.onResume();
        setupBottomSheetDialogMaxWidth(this);

        int textResId;
        boolean enabled;
        if (ExternalEqualizer.isExternalEqualizerExists(requireContext())) {
            textResId = R.string.external_equalizer_description;
            enabled = true;
        } else {
            textResId = R.string.external_equalizer_not_found;
            enabled = false;
        }
        viewBinding.btnOpenSystemEqualizer.setEnabled(enabled);
        viewBinding.rbUseSystemEqualizer.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setText(getString(textResId));
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        AppSnackbar.make(viewBinding.getRoot(), errorCommand.getMessage()).show();
    }

    @Override
    public void displayEqualizerConfig(EqualizerConfig equalizerConfig) {
        for (Band band: equalizerConfig.getBands()) {

            PartialEqualizerBandBinding binding = PartialEqualizerBandBinding.inflate(LayoutInflater.from(getContext()));
            bandsViewList.add(new Pair<>(binding, band));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.weight = 1;
            viewBinding.llBands.addView(binding.getRoot(), lp);

            short lowestRange = equalizerConfig.getLowestBandRange();
            short highestRange = equalizerConfig.getHighestBandRange();
            int max = highestRange - lowestRange;
            binding.sbLevel.setMax(max);
            binding.sbLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        short value = (short) (progress - Math.abs(lowestRange));
                        binding.tvLevel.setText(FormatUtils.formatDecibels(value));
                        presenter.onBandLevelChanged(band, value);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });


            binding.tvFrequency.setText(FormatUtils.formatHz(band.getFrequencyRange()[1]));
        }

        List<Preset> presets = equalizerConfig.getPresets();
        ArrayAdapter<Preset> adapter = new ArrayAdapter<>(requireContext(),
                R.layout.item_autocomplete,
                R.id.text_view,
                presets);
        viewBinding.spinnerPresets.setAdapter(adapter);

        setInAppEqualizerSettingsEnabled(inAppEqualizerSettingsEnabled);
//        viewBinding.spinnerPresets.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                presenter.onPresetSelected(presets.get(position));
//            }
//        });
//        viewBinding.spinnerPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                presenter.onPresetSelected(presets.get(i));
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });
    }

    @Override
    public void displayEqualizerState(EqualizerState equalizerState, EqualizerConfig config) {
        int position = ListUtils.findPosition(
                config.getPresets(),
                preset -> preset.getNumber() == equalizerState.getCurrentPreset()
        );
        viewBinding.spinnerPresets.setSelection(position);

        for (Pair<PartialEqualizerBandBinding, Band> pair: bandsViewList) {
            PartialEqualizerBandBinding binding = pair.first;
            Band band = pair.second;

            Short currentRange = equalizerState.getBendLevels().get(band.getBandNumber());
            if (currentRange == null) {
                return;
            }
            binding.tvLevel.setText(FormatUtils.formatDecibels(currentRange));
            short lowestRange = config.getLowestBandRange();
            binding.sbLevel.setProgress(currentRange + Math.abs(lowestRange));
        }

    }

    private void enableSystemEqualizer() {
        equalizerController.enableEqualizer(EqualizerType.EXTERNAL);
        showActiveEqualizer(EqualizerType.EXTERNAL);
    }

    private void enableAppEqualizer() {
        equalizerController.enableEqualizer(EqualizerType.APP);
        showActiveEqualizer(EqualizerType.APP);
    }

    private void openSystemEqualizer() {
        equalizerController.launchExternalEqualizerSetup(getActivity());
        showActiveEqualizer(EqualizerType.EXTERNAL);
    }

    private void disableEqualizer() {
        equalizerController.disableEqualizer();
        showActiveEqualizer(EqualizerType.NONE);
    }

    private void showActiveEqualizer(int type) {
        viewBinding.rbUseSystemEqualizer.setChecked(type == EqualizerType.EXTERNAL);
        viewBinding.rbUseAppEqualizer.setChecked(type == EqualizerType.APP);
        viewBinding.rbDisableEqualizer.setChecked(type == EqualizerType.NONE);

        setInAppEqualizerSettingsEnabled(type == EqualizerType.APP);
    }

    private void setInAppEqualizerSettingsEnabled(boolean enabled) {
        this.inAppEqualizerSettingsEnabled = enabled;

        for (Pair<PartialEqualizerBandBinding, Band> pair: bandsViewList) {
            PartialEqualizerBandBinding binding = pair.first;
            binding.sbLevel.setEnabled(enabled);
        }
        viewBinding.spinnerPresets.setEnabled(enabled);
    }

}
