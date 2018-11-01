package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

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
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PlayListsFragment extends MvpAppCompatFragment
        implements PlayListsView, FragmentLayerListener {

    @InjectPresenter
    PlayListsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

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

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.play_lists);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
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
    public void updateList(ListUpdate<PlayList> update) {
        List<PlayList> list = update.getNewList();
        if (adapter == null) {
            adapter = new PlayListsAdapter(list);
            adapter.setOnItemClickListener(this::goToPlayListScreen);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(list);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    private void goToPlayListScreen(PlayList playList) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(() -> PlayListFragment.newInstance(playList.getId()));
    }
}
