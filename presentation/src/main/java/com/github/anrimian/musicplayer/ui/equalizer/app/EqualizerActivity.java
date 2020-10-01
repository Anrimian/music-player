package com.github.anrimian.musicplayer.ui.equalizer.app;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ActivityEqualizerBinding;
import com.github.anrimian.musicplayer.databinding.PartialEqualizerBandBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.equalizer.Band;
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
    public void displayBands(List<Band> bands) {
        for (Band band: bands) {
            PartialEqualizerBandBinding binding = PartialEqualizerBandBinding.inflate(getLayoutInflater());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);
            lp.weight = 1;
            viewBinding.llBands.addView(binding.getRoot(), lp);

            short currentRange = band.getCurrentRange();
            binding.tvLevel.setText(String.valueOf(currentRange));

            short lowestRange = band.getLevelRange()[0];//check
            short highestRange = band.getLevelRange()[1];
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

            binding.tvFrequency.setText(String.valueOf(band.getFrequencyRange()[0]));
        }

    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        AppSnackbar.make(viewBinding.container, errorCommand.getMessage()).show();
    }
}
