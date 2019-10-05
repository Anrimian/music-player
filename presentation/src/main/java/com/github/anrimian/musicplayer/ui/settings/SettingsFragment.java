package com.github.anrimian.musicplayer.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.display.DisplaySettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.headset.HeadsetSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment;
import com.github.anrimian.musicplayer.ui.settings.themes.ThemeSettingsFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class SettingsFragment extends Fragment implements FragmentLayerListener {

    @BindView(R.id.fl_container)
    View flContainer;

    @BindView(R.id.tv_display)
    TextView tvDisplay;

    @BindView(R.id.tv_player)
    TextView tvPlayer;

    @BindView(R.id.tv_theme_name)
    TextView tvTheme;

    @BindView(R.id.tv_headset)
    TextView tvHeadset;

    private FragmentNavigation navigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(requireFragmentManager());

        tvDisplay.setOnClickListener(v -> navigation.addNewFragment(new DisplaySettingsFragment()));
        tvPlayer.setOnClickListener(v -> navigation.addNewFragment(new PlayerSettingsFragment()));
        tvTheme.setOnClickListener(v -> navigation.addNewFragment(new ThemeSettingsFragment()));
        tvHeadset.setOnClickListener(v -> navigation.addNewFragment(new HeadsetSettingsFragment()));

        SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
        SlidrPanel.replace(flContainer,
                slidrConfig,
                () -> navigation.goBack(0),
                toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
    }
}
