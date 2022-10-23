package com.github.anrimian.musicplayer.ui.settings.themes;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsThemesBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme;
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.themes.view.ThemesAdapter;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.r0adkll.slidr.model.SlidrInterface;

public class ThemeSettingsFragment extends Fragment {

    private FragmentSettingsThemesBinding viewBinding;

    private SlidrInterface slidrInterface;

    private ThemeController themeController;

    private ThemesAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsThemesBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        themeController = Components.getAppComponent().themeController();

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.theme);
        toolbar.setTitleClickListener(null);

        slidrInterface = SlidrPanel.simpleSwipeBack(viewBinding.nsvContainer, this, toolbar::onStackFragmentSlided);

        viewBinding.rvThemes.setLayoutManager(new GridLayoutManager(requireContext(), 2, RecyclerView.HORIZONTAL, false));
        viewBinding.rvThemes.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                onAlbumsScrolled(viewBinding.rvThemes.computeHorizontalScrollOffset() == 0);
            }
        });

        adapter = new ThemesAdapter(AppTheme.appThemes(),
                themeController.getCurrentTheme(),
                this::onThemeClicked);
        viewBinding.rvThemes.setAdapter(adapter);

        setChecked(viewBinding.cbAutoNightMode, themeController.isAutoDarkThemeEnabled());
        viewBinding.cbAutoNightMode.setOnCheckedChangeListener((v, isChecked) ->
                themeController.setAutoDarkModeEnabled(requireActivity(), isChecked)
        );
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            setChecked(viewBinding.cbFollowSystemTheme, themeController.isFollowSystemThemeEnabled());
            viewBinding.cbFollowSystemTheme.setOnCheckedChangeListener((v, isChecked) ->
                    themeController.setFollowSystemThemeEnabled(requireActivity(), isChecked)
            );
        } else {
            viewBinding.cbFollowSystemTheme.setVisibility(View.GONE);
        }
    }

    private void onAlbumsScrolled(boolean onStart) {
        if (onStart) {
            slidrInterface.unlock();
        } else {
            slidrInterface.lock();
        }
    }

    private void onThemeClicked(AppTheme appTheme) {
        viewBinding.cbFollowSystemTheme.setChecked(false);
        themeController.setTheme(requireActivity(), appTheme);
        adapter.setCurrentTheme(appTheme);
    }
}
