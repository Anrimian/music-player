package com.github.anrimian.musicplayer.ui.playlist_screens.playlist;

import static com.github.anrimian.musicplayer.Constants.Arguments.PLAY_LIST_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.POSITION_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeletePlayListItemCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.makeSnackbar;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentBaseFabListBinding;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem;
import com.github.anrimian.musicplayer.domain.models.utils.ListPosition;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.composition.CompositionActionDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.common.view.ViewUtils;
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler;
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler;
import com.github.anrimian.musicplayer.ui.editor.composition.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter.PlayListItemAdapter;
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_swipe.DragAndSwipeTouchHelperCallback;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

public class PlayListFragment extends MvpAppCompatFragment
        implements PlayListView, FragmentLayerListener {

    @InjectPresenter
    PlayListPresenter presenter;

    private FragmentBaseFabListBinding viewBinding;

    private RecyclerView recyclerView;
    private View fab;
    private CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private PlayListItemAdapter adapter;
    private LinearLayoutManager layoutManager;

    private DialogFragmentRunner<CompositionActionDialogFragment> compositionActionDialogRunner;

    private ErrorHandler deletingErrorHandler;

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
        viewBinding = FragmentBaseFabListBinding.inflate(inflater, container, false);
        recyclerView = viewBinding.recyclerView;
        fab = viewBinding.fab;
        clListContainer = viewBinding.listContainer;
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitleClickListener(null);

        DragAndSwipeTouchHelperCallback callback = FormatUtils.withSwipeToDelete(recyclerView,
                getColorFromAttr(requireContext(), R.attr.listItemBottomBackground),
                presenter::onItemSwipedToDelete,
                ItemTouchHelper.START,
                R.drawable.ic_playlist_remove,
                R.string.delete_from_play_list);
        callback.setOnMovedListener(presenter::onItemMoved);
        callback.setOnStartDragListener(presenter::onDragStarted);
        callback.setOnEndDragListener(presenter::onDragEnded);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewUtils.attachFastScroller(recyclerView, true);

        adapter = new PlayListItemAdapter(recyclerView,
                presenter.isCoversEnabled(),
                presenter::onCompositionClicked,
                presenter::onItemIconClicked);
        recyclerView.setAdapter(adapter);

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        SlidrPanel.simpleSwipeBack(clListContainer, this, toolbar::onStackFragmentSlided);

        FragmentManager fm = getChildFragmentManager();

        deletingErrorHandler = new DeleteErrorHandler(getChildFragmentManager(),
                presenter::onRetryFailedDeleteActionClicked,
                this::showEditorRequestDeniedMessage);

        ChoosePlayListDialogFragment playListDialog = (ChoosePlayListDialogFragment) fm
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (playListDialog != null) {
            playListDialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        }

        compositionActionDialogRunner = new DialogFragmentRunner<>(fm,
                COMPOSITION_ACTION_TAG,
                f -> f.setOnTripleCompleteListener(this::onCompositionActionSelected));
    }

    @Override
    public void onFragmentMovedOnTop() {
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setupOptionsMenu(R.menu.play_list_menu, this::onOptionsItemClicked);

        presenter.onFragmentMovedToTop();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop(ViewUtils.getListPosition(layoutManager));
    }

    @Override
    public void showEmptyList() {
        fab.setVisibility(View.GONE);
        viewBinding.progressStateView.showMessage(R.string.play_list_is_empty, false);
    }

    @Override
    public void showList() {
        fab.setVisibility(View.VISIBLE);
        viewBinding.progressStateView.hideAll();
    }

    @Override
    public void showLoading() {
        viewBinding.progressStateView.showProgress();
    }

    @Override
    public void updateItemsList(List<PlayListItem> list) {
        adapter.submitList(list);
    }

    @Override
    public void restoreListPosition(ListPosition listPosition) {
        ViewUtils.scrollToPosition(layoutManager, listPosition);
    }

    @Override
    public void closeScreen() {
        FragmentNavigation.from(getParentFragmentManager()).goBack();
    }

    @Override
    public void showPlayListInfo(PlayList playList) {
        toolbar.setTitle(playList.getName());
        toolbar.setSubtitle(formatCompositionsCount(requireContext(), playList.getCompositionsCount()));
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
        deletingErrorHandler.handleError(errorCommand, () ->
                makeSnackbar(clListContainer,
                        getString(R.string.delete_composition_error_template, errorCommand.getMessage()),
                        Snackbar.LENGTH_SHORT)
                        .show()
        );
    }

    @Override
    public void showDeletedCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, List<Composition> compositions) {
        String text = getAddToPlayListCompleteMessage(requireActivity(), playList, compositions);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showDeleteItemError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.add_item_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteItemCompleted(PlayList playList, List<PlayListItem> items) {
        String text = getDeletePlayListItemCompleteMessage(requireActivity(), playList, items);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_LONG)
                .setAction(R.string.cancel, presenter::onRestoreRemovedItemClicked)
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
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.play_list_deleted, playList.getName()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeletePlayListError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
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
    public void showCompositionActionDialog(PlayListItem playListItem, int position) {
        Bundle extra = new Bundle();
        extra.putLong(PLAY_LIST_ID_ARG, playListItem.getItemId());
        extra.putInt(POSITION_ARG, position);
        Composition composition = playListItem.getComposition();
        CompositionActionDialogFragment fragment = CompositionActionDialogFragment.newInstance(
                composition,
                R.menu.play_list_item_menu,
                extra);
        compositionActionDialogRunner.show(fragment);
    }

    @Override
    public void showEditPlayListNameDialog(PlayList playList) {
        RenamePlayListDialogFragment fragment =
                RenamePlayListDialogFragment.newInstance(playList.getId());
        fragment.show(getChildFragmentManager(), null);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCompositionsAddedToPlayNext(List<Composition> compositions) {
        String message = MessagesUtils.getPlayNextMessage(requireContext(), compositions);
        MessagesUtils.makeSnackbar(clListContainer, message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onCompositionsAddedToQueue(List<Composition> compositions) {
        String message = MessagesUtils.getAddedToQueueMessage(requireContext(), compositions);
        MessagesUtils.makeSnackbar(clListContainer, message, Snackbar.LENGTH_SHORT).show();
    }

    private void onCompositionActionSelected(Composition composition,
                                             @MenuRes int menuItemId,
                                             Bundle extra) {
        long playListId = extra.getLong(PLAY_LIST_ID_ARG);
        int position = extra.getInt(POSITION_ARG);

        switch (menuItemId) {
            case R.id.menu_play: {
                presenter.onPlayActionSelected(position);
                break;
            }
            case R.id.menu_play_next: {
                presenter.onPlayNextCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_queue: {
                presenter.onAddToQueueCompositionClicked(composition);
                break;
            }
            case R.id.menu_add_to_playlist: {
                presenter.onAddToPlayListButtonClicked(composition);
                break;
            }
            case R.id.menu_edit: {
                startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.getId()));
                break;
            }
            case R.id.menu_share: {
                DialogUtils.shareComposition(requireContext(), composition);
                break;
            }
            case R.id.menu_delete_from_play_list: {
                presenter.onDeleteFromPlayListButtonClicked(
                        new PlayListItem(playListId, composition));
                break;
            }
            case R.id.menu_delete: {
                presenter.onDeleteCompositionButtonClicked(composition);
                break;
            }
        }
    }

    private long getPlayListId() {
        return requireArguments().getLong(PLAY_LIST_ID_ARG);
    }

    private void onOptionsItemClicked(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_change_play_list_name: {
                presenter.onChangePlayListNameButtonClicked();
                break;
            }
            case R.id.menu_delete_play_list: {
                presenter.onDeletePlayListButtonClicked();
                break;
            }
        }
    }

    private void showEditorRequestDeniedMessage() {
        makeSnackbar(clListContainer, R.string.android_r_edit_file_permission_denied, Snackbar.LENGTH_LONG).show();
    }
}
