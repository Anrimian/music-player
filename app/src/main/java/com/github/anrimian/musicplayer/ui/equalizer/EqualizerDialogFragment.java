package com.github.anrimian.musicplayer.ui.equalizer;

import static android.view.View.MeasureSpec.makeMeasureSpec;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.setupBottomSheetDialogMaxWidth;
import static com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils.attachDynamicShadow;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.util.Pair;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.menu.SimpleMenuBuilder;
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.util.LinkedList;
import java.util.List;

import moxy.MvpBottomSheetDialogFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

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

        view.measure(
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        );

        BottomSheetBehavior<FrameLayout> bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog);
        bottomSheetBehavior.addBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss();
            }
        }, o -> {}));
        bottomSheetBehavior.setPeekHeight(view.getMeasuredHeight());
        bottomSheetBehavior.setSkipCollapsed(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground);

        attachDynamicShadow(viewBinding.nestedScrollView, viewBinding.titleShadow);

        equalizerController = Components.getAppComponent().equalizerController();

        viewBinding.rbUseSystemEqualizer.setOnClickListener(v -> enableSystemEqualizer());
        viewBinding.btnOpenSystemEqualizer.setOnClickListener(v -> openSystemEqualizer());
        viewBinding.rbUseAppEqualizer.setOnClickListener(v -> enableAppEqualizer());
        viewBinding.rbDisableEqualizer.setOnClickListener(v -> disableEqualizer());
        viewBinding.ivClose.setOnClickListener(v -> dismissAllowingStateLoss());
        viewBinding.btnRestartSystemEqualizer.setOnClickListener(v -> presenter.onRestartAppEqClicked());

        showActiveEqualizer(equalizerController.getSelectedEqualizerType());

        CompatUtils.setOutlineButtonStyle(viewBinding.btnOpenSystemEqualizer);
        CompatUtils.setOutlineButtonStyle(viewBinding.tvPresets);
        CompatUtils.setOutlineButtonStyle(viewBinding.btnRestartSystemEqualizer);
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
        viewBinding.tvSystemEqualizerText.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setEnabled(enabled);
        viewBinding.tvSystemEqualizerDescription.setText(getString(textResId));
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        Toast.makeText(requireContext(), errorCommand.getMessage(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void displayEqualizerConfig(EqualizerConfig equalizerConfig) {
        for (Band band: equalizerConfig.getBands()) {

            PartialEqualizerBandBinding binding = PartialEqualizerBandBinding.inflate(LayoutInflater.from(getContext()));
            bandsViewList.add(new Pair<>(binding, band));

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.weight = 1;
            viewBinding.llBands.addView(binding.getRoot(), lp);//recalculate dialog height?

            short lowestRange = equalizerConfig.getLowestBandRange();
            short highestRange = equalizerConfig.getHighestBandRange();
            int max = highestRange - lowestRange;
            binding.sbLevel.setMax(max);
            SeekBarViewWrapper seekBarViewWrapper = new SeekBarViewWrapper(binding.sbLevel);
            seekBarViewWrapper.setProgressChangeListener(progress -> {
                short value = (short) (progress - Math.abs(lowestRange));
                binding.tvLevel.setText(FormatUtils.formatDecibels(value));
                presenter.onBandLevelChanged(band, value);
            });
            seekBarViewWrapper.setOnSeekStopListener(progress -> presenter.onBandLevelDragStopped());

            binding.tvFrequency.setText(FormatUtils.formatMilliHz(band.getCenterFreq()));
        }

        List<Preset> presets = equalizerConfig.getPresets();
        SimpleMenuBuilder menuBuilder = new SimpleMenuBuilder(requireContext());
        for (int i = 0; i < presets.size(); i++) {
            Preset preset = presets.get(i);
            menuBuilder.add(i, preset.getPresetName());
        }

        viewBinding.tvPresets.setOnClickListener(v ->
                PopupMenuWindow.showActionBarPopup(viewBinding.tvPresets,
                        menuBuilder.getItems(),
                        menu -> presenter.onPresetSelected(presets.get(menu.getItemId())),
                        Gravity.END
                )
        );

        setInAppEqualizerSettingsEnabled(inAppEqualizerSettingsEnabled);
    }

    @Override
    public void displayEqualizerState(EqualizerState equalizerState, EqualizerConfig config) {
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

    @Override
    public void showEqualizerRestartButton(boolean show) {
        viewBinding.btnRestartSystemEqualizer.setVisibility(show? View.VISIBLE : View.GONE);
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
        viewBinding.tvPresets.setEnabled(enabled);
    }

}
