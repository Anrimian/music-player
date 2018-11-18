package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
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
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter.PlayListItemAdapter;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_LIST_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.DialogUtils.shareFile;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeletePlayListItemCompleteMessage;

public class PlayListFragment extends MvpAppCompatFragment implements PlayListView {

    @InjectPresenter
    PlayListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    View fab;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private PlayListItemAdapter adapter;
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
        SlidrPanel.replace(clListContainer, slidrConfig, () ->
                FragmentNavigation.from(requireFragmentManager()).goBack(0),
                toolbar::onStackFragmentSlided);

        ChoosePlayListDialogFragment playListDialog = (ChoosePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (playListDialog != null) {
            playListDialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        }
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
    public void updateItemsList(ListUpdate<PlayListItem> update) {
        List<PlayListItem> list = update.getNewList();
        if (adapter == null) {
            adapter = new PlayListItemAdapter(list);
            adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
            adapter.setOnMenuItemClickListener(this::onCompositionMenuClicked);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(list);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    @Override
    public void closeScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    @Override
    public void showPlayListInfo(PlayList playList) {
        toolbar.setTitle(playList.getName());
        toolbar.setSubtitle(getResources().getQuantityString(
                R.plurals.compositions_count,
                playList.getCompositionsCount(),
                playList.getCompositionsCount()));
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_TAG);
    }


    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        Snackbar.make(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions) {
        String text = getAddToPlayListCompleteMessage(requireActivity(), playList, compositions);
        Snackbar.make(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteItemError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.add_item_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteItemCompleted(PlayList playList, List<PlayListItem> items) {
        String text = getDeletePlayListItemCompleteMessage(requireActivity(), playList, items);
        Snackbar.make(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    private void onCompositionMenuClicked(View view, PlayListItem playListItem) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.play_list_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    presenter.onAddToPlayListButtonClicked(playListItem.getComposition());
                    return true;
                }
                case R.id.menu_share: {
                    shareFile(requireContext(), playListItem.getComposition().getFilePath());
                    return true;
                }
                case R.id.menu_delete_from_play_list: {
                    presenter.onDeleteFromPlayListButtonClicked(playListItem);
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteCompositionButtonClicked(playListItem.getComposition());
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    private long getPlayListId() {
        return Objects.requireNonNull(getArguments()).getLong(PLAY_LIST_ID_ARG);
    }
}
