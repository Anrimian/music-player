package com.github.anrimian.musicplayer.ui.equalizer.app;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ActivityEqualizerBinding;
import com.github.anrimian.musicplayer.databinding.PartialEqualizerBandBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerInfo;
import com.github.anrimian.musicplayer.domain.models.equalizer.Preset;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.r0adkll.slidr.Slidr;

import java.util.List;

import moxy.MvpAppCompatActivity;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

public class EqualizerActivity extends MvpAppCompatActivity implements EqualizerView {

    @InjectPresenter
    EqualizerPresenter presenter;

    private ActivityEqualizerBinding viewBinding;

    @ProvidePresenter
    EqualizerPresenter providePresenter() {
        return Components.getAppComponent().equalizerPresenter();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        viewBinding = ActivityEqualizerBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground);

        setSupportActionBar(viewBinding.toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.equalizer);
        }

        Slidr.attach(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void displayEqualizerInfo(EqualizerInfo equalizerInfo) {
        for (Band band: equalizerInfo.getBands()) {
            PartialEqualizerBandBinding binding = PartialEqualizerBandBinding.inflate(getLayoutInflater());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.weight = 1;
            viewBinding.llBands.addView(binding.getRoot(), lp);

            short currentRange = band.getCurrentRange();
            binding.tvLevel.setText(String.valueOf(currentRange));

            short lowestRange = equalizerInfo.getBandLevelRange()[0];//check
            short highestRange = equalizerInfo.getBandLevelRange()[1];
            int max = highestRange - lowestRange;
            binding.sbLevel.setMax(max);
            binding.sbLevel.setProgress(currentRange + Math.abs(lowestRange));
            binding.sbLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        int value = progress - Math.abs(lowestRange);
                        binding.tvLevel.setText(String.valueOf(value));
                        presenter.onBandLevelChanged(band, (short) value);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });

            binding.tvFrequency.setText(String.valueOf(band.getFrequencyRange()[1]));
        }

        //display presents

        List<Preset> presets = equalizerInfo.getPresets();
        ArrayAdapter<Preset> adapter = new ArrayAdapter<>(this,
                R.layout.item_autocomplete,
                R.id.text_view,
                presets);
        viewBinding.spinnerPresets.setAdapter(adapter);
        viewBinding.spinnerPresets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                presenter.onPresetSelected(presets.get(i));
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        AppSnackbar.make(viewBinding.container, errorCommand.getMessage()).show();
    }
}
