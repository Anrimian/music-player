package com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
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
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlist.PlayListFragment;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.simplemusicplayer.ui.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayListsFragment extends MvpAppCompatFragment implements PlayListsView {

    @InjectPresenter
    PlayListsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private AdvancedToolbar toolbar;

    private PlayListsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    @ProvidePresenter
    PlayListsPresenter providePresenter() {
        return Components.getAppComponent().playListsPresenter();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_lists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar = getActivity().findViewById(R.id.toolbar);
        bindToolbar(toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FragmentManager fm = getFragmentManager();
        fm.addOnBackStackChangedListener(() -> {
            Fragment fragment = fm.findFragmentById(R.id.drawer_fragment_container);
            if (fragment == this) {
                bindToolbar(toolbar);
            }
        });
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(R.string.play_lists_on_device_not_found, false);
    }

    @Override
    public void showList() {
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        progressViewWrapper.showProgress();
    }

    @Override
    public void bindList(List<PlayList> playLists) {
        adapter = new PlayListsAdapter(playLists);
        adapter.setOnItemClickListener(this::goToPlayListScreen);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateList(List<PlayList> oldList, List<PlayList> newList) {
        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        adapter.updateList(oldList, newList);
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    private void goToPlayListScreen(PlayList playList) {
        getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_alpha_appear, 0, 0, R.anim.anim_alpha_disappear)
                .add(R.id.drawer_fragment_container, PlayListFragment.newInstance(playList.getId()))
                .addToBackStack(null)
                .commit();
    }

    private void bindToolbar(AdvancedToolbar toolbar) {
        toolbar.setTitle(R.string.play_lists);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
    }
}
