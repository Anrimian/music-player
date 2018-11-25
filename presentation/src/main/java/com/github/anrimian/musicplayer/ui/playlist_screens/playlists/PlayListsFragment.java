package com.github.anrimian.musicplayer.ui.playlist_screens.playlists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatFragment;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Tags.PLAY_LIST_MENU;

public class PlayListsFragment extends MvpAppCompatFragment
        implements PlayListsView, FragmentLayerListener {

    @InjectPresenter
    PlayListsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

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

        MenuDialogFragment fragment = (MenuDialogFragment) getChildFragmentManager()
                .findFragmentByTag(PLAY_LIST_MENU);
        if (fragment != null) {
            fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        }
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
            adapter.setOnItemLongClickListener(presenter::onPlayListLongClick);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(list);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    @Override
    public void showPlayListMenu(PlayList playList) {
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(R.menu.play_list_menu, playList.getName());
        fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        fragment.show(getChildFragmentManager(), PLAY_LIST_MENU);
    }

    @Override
    public void showConfirmDeletePlayListDialog(PlayList playList) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                playList,
                presenter::onDeletePlayListDialogConfirmed);
    }

    @Override
    public void showPlayListDeleteSuccess(PlayList playList) {
        Snackbar.make(clListContainer,
                getString(R.string.play_list_deleted, playList.getName()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeletePlayListError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.play_list_delete_error, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void onPlayListMenuItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete: {
                presenter.onDeletePlayListButtonClicked();
                break;
            }
        }
    }

    private void goToPlayListScreen(PlayList playList) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(() -> PlayListFragment.newInstance(playList.getId()));
    }
}
