package com.github.anrimian.simplemusicplayer.ui.library.storage;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.files.FileSource;
import com.github.anrimian.simplemusicplayer.ui.library.storage.view.adapter.MusicFileSourceAdapter;
import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.constants.Arguments.PATH;

/**
 * Created on 23.10.2017.
 */

public class StorageLibraryFragment extends MvpAppCompatFragment implements StorageLibraryView {

    @InjectPresenter
    StorageLibraryPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private ProgressViewWrapper progressViewWrapper;
    private MusicFileSourceAdapter adapter;

    public StorageLibraryFragment newInstance(@Nullable String path) {
        Bundle args = new Bundle();
        args.putString(PATH, path);
        StorageLibraryFragment fragment = new StorageLibraryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private String getPath() {
        return getArguments().getString(PATH);
    }

    @ProvidePresenter
    StorageLibraryPresenter providePresenter() {
        return Components.getStorageLibraryComponent(getPath()).storageLibraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_storage, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.setTryAgainButtonOnClickListener(v -> presenter.onTryAgainButtonClicked());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MusicFileSourceAdapter();
    }

    @Override
    public void showBackPathButton(@NonNull String path) {

    }

    @Override
    public void hideBackPathButton() {

    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.showMessage(R.string.there_is_nothing_here, false);
    }

    @Override
    public void showMusicList(List<FileSource> musicList) {
        adapter.setMusicList(musicList);
        adapter.notifyItemRangeInserted(0, musicList.size());
    }

    @Override
    public void showLoading() {
        progressViewWrapper.showProgress();
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);//TODO add default handler
    }

    @Override
    public void goToMusicStorageScreen(String path) {

    }
}
