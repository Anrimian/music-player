package com.github.anrimian.musicplayer.ui.library.folders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LibraryFoldersRootFragment extends LibraryFragment implements BackButtonListener {

    @BindView(R.id.library_folders_container)
    JugglerView jvFoldersContainer;

    private FragmentNavigation navigation;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_root_library_folders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.files);

        navigation = FragmentNavigation.from(getChildFragmentManager());
        navigation.initialize(jvFoldersContainer, savedInstanceState);
        navigation.setExitAnimation(R.anim.anim_slide_out_right);
        navigation.setEnterAnimation(R.anim.anim_slide_in_right);

        if (!navigation.hasScreens()) {
            navigation.newRootFragment(() -> LibraryFoldersFragment.newInstance(null));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        navigation.onSaveInstanceState(outState);
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = navigation.getFragmentOnTop();
        return fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
    }
}
