package com.github.anrimian.simplemusicplayer.ui.library.storage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.MusicFileSource;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;

import java.util.List;

/**
 * Created on 23.10.2017.
 */

public class StorageLibraryFragment extends MvpAppCompatFragment implements StorageLibraryView {

    @InjectPresenter
    StorageLibraryPresenter presenter;

    @ProvidePresenter
    StorageLibraryPresenter providePresenter() {
        return new StorageLibraryPresenter(null);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_storage, container, false);
    }

    @Override
    public void showEmptyList() {

    }

    @Override
    public void showMusicList(List<MusicFileSource> musicList) {

    }

    @Override
    public void showLoading() {

    }

    @Override
    public void showError(ErrorCommand errorCommand) {

    }

    @Override
    public void goToMusicStorageScreen(String path) {

    }
}
