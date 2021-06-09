package com.github.anrimian.musicplayer.ui.settings.library;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentLibrarySettingsBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment;
import com.github.anrimian.musicplayer.ui.utils.ViewUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

/**
 * Created on 19.10.2017.
 */

public class LibrarySettingsFragment extends MvpAppCompatFragment
        implements FragmentLayerListener, LibrarySettingsView {

    @InjectPresenter
    LibrarySettingsPresenter presenter;

    private FragmentLibrarySettingsBinding viewBinding;

    private FragmentNavigation navigation;

    @ProvidePresenter
    LibrarySettingsPresenter providePresenter() {
        return Components.getSettingsComponent().librarySettingsPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentLibrarySettingsBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);

        navigation = FragmentNavigation.from(getParentFragmentManager());

        viewBinding.tvExcludedFolders.setOnClickListener(v -> navigation.addNewFragment(new ExcludedFoldersFragment()));

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            viewBinding.cbDoNotShowDeleteDialog.setVisibility(View.GONE);
        }
        ViewUtils.onCheckChanged(viewBinding.cbDoNotShowDeleteDialog, presenter::doNotAppConfirmDialogChecked);

        SlidrPanel.simpleSwipeBack(viewBinding.flContainer, this, toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.library);
        toolbar.setTitleClickListener(null);
    }

    @Override
    public void showAppConfirmDeleteDialogEnabled(boolean enabled) {
        ViewUtils.setChecked(viewBinding.cbDoNotShowDeleteDialog, !enabled);
    }

}
