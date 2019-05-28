package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

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
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatFragment;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_LIST_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.DialogUtils.shareFile;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeletePlayListItemCompleteMessage;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback.withSwipeToDelete;

public class PlayListFragment extends MvpAppCompatFragment
        implements PlayListView, FragmentLayerListener {

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
        progressViewWrapper.hideAll();

        DragAndSwipeTouchHelperCallback callback = withSwipeToDelete(recyclerView,
                getColorFromAttr(requireContext(), R.attr.listBackground),
                presenter::onItemSwipedToDelete,
                ItemTouchHelper.END);
        callback.setOnMovedListener(presenter::onItemMoved);
        callback.setOnStartDragListener(presenter::onDragStarted);
        callback.setOnEndDragListener(presenter::onDragEnded);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

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
    public void onFragmentMovedOnTop() {
        presenter.onFragmentMovedToTop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.play_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_change_play_list_name: {
                presenter.onChangePlayListNameButtonClicked();
                return true;
            }
            case R.id.menu_delete_play_list: {
                presenter.onDeletePlayListButtonClicked();
                return true;
            }
            default: return super.onOptionsItemSelected(item);
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
        setHasOptionsMenu(true);
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
    public void showDeletedCompositionMessage(List<Composition> compositionsToDelete) {
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
        Snackbar.make(clListContainer, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, v -> presenter.onRestoreRemovedItemClicked())
                .show();
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

    @Override
    public void notifyItemMoved(int from, int to) {
        adapter.notifyItemMoved(from, to);
    }

    @Override
    public void notifyItemRemoved(int position) {
        adapter.notifyItemRemoved(position);
    }

    @Override
    public void showCompositionActionDialog(Composition composition) {
        MenuDialogFragment menuDialogFragment = MenuDialogFragment.newInstance(
                R.menu.composition_actions_menu,
                formatCompositionName(composition)
        );
        menuDialogFragment.setOnCompleteListener(this::onCompositionActionSelected);
        menuDialogFragment.show(getChildFragmentManager(), COMPOSITION_ACTION_TAG);
    }

    @Override
    public void showEditPlayListNameDialog(PlayList playList) {
        RenamePlayListDialogFragment fragment =
                RenamePlayListDialogFragment.newInstance(playList.getId());
        fragment.show(getChildFragmentManager(), null);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    private void onCompositionActionSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_play: {
                presenter.onPlayActionSelected();
                break;
            }
            case R.id.menu_play_next: {
                presenter.onPlayNextActionSelected();
                break;
            }
            case R.id.menu_add_to_queue: {
                presenter.onAddToQueueActionSelected();
                break;
            }
        }
    }

    private void onCompositionMenuClicked(View view, PlayListItem playListItem, int position) {
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
                    presenter.onDeleteFromPlayListButtonClicked(playListItem, position);
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
