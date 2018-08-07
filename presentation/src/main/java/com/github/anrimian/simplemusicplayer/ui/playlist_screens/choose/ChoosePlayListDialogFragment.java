package com.github.anrimian.simplemusicplayer.ui.playlist_screens.choose;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.PlayListsPresenter;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.PlayListsView;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.simplemusicplayer.ui.utils.moxy.MvpBottomSheetDialogFragment;
import com.github.anrimian.simplemusicplayer.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChoosePlayListDialogFragment extends MvpBottomSheetDialogFragment
        implements ChoosePlayListView {

    @InjectPresenter
    ChoosePlayListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.iv_create_playlist)
    ImageView ivCreatePlaylist;

    private PlayListsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    @ProvidePresenter
    ChoosePlayListPresenter providePresenter() {
        return Components.getPlayListsComponent().choosePlayListPresenter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.dialog_select_play_list, null);
        dialog.setContentView(view);

        DisplayMetrics displayMetrics = getActivity().getResources().getDisplayMetrics();

        int height = displayMetrics.heightPixels;

        int minHeight = (int) (height * 0.6);
        view.setMinimumHeight(minHeight);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(minHeight);

        ButterKnife.bind(this, view);

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        ivCreatePlaylist.setOnClickListener(v -> onCreatePlayListButtonClicked());
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
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateList(List<PlayList> oldList, List<PlayList> newList) {
        adapter.updateList(oldList, newList);
    }

    private void onCreatePlayListButtonClicked() {
        CreatePlayListDialogFragment fragment = new CreatePlayListDialogFragment();
        fragment.show(getChildFragmentManager(), null);
    }
}
