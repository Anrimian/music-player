package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
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
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrFragment;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_LIST_ID_ARG;

public class PlayListFragment extends MvpAppCompatFragment implements PlayListView {

    @InjectPresenter
    PlayListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private CompositionsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    public static PlayListFragment newInstance(long playListId) {
        Bundle args = new Bundle();
        args.putLong(PLAY_LIST_ID_ARG, playListId);
        PlayListFragment fragment = new PlayListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    PlayListPresenter providePresenter() {
        return Components.getPlayListComponent(getPlayListId()).playListPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ButterKnife.bind(this, view);

        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitleClickListener(null);

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
        SlidrFragment.replace(this, clListContainer, slidrConfig, toolbar::onStackFragmentSlided);
    }

    @Override
    public void showEmptyList() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(R.string.play_list_is_empty, false);
    }

    @Override
    public void showList() {
        fab.setVisibility(View.VISIBLE);
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        progressViewWrapper.showProgress();
    }

    @Override
    public void bindList(List<Composition> compositions) {
        adapter = new CompositionsAdapter(compositions);
        adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
//        adapter.setOnDeleteCompositionClickListener(presenter::onDeleteCompositionButtonClicked);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateList(List<Composition> oldList, List<Composition> newList) {
        DiffUtilHelper.update(oldList, newList, CompositionHelper::areSourcesTheSame, recyclerView);
    }

    @Override
    public void closeScreen() {
        requireFragmentManager().popBackStack();
    }

    @Override
    public void showPlayListInfo(PlayList playList) {
        toolbar.setTitle(playList.getName());
        toolbar.setSubtitle(getResources().getQuantityString(
                R.plurals.compositions_count,
                playList.getCompositionsCount(),
                playList.getCompositionsCount()));
    }

    private long getPlayListId() {
        return Objects.requireNonNull(getArguments()).getLong(PLAY_LIST_ID_ARG);
    }
}
