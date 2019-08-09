package com.github.anrimian.musicplayer.ui.library.folders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.CompositionEditorActivity;
import com.github.anrimian.musicplayer.ui.library.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.library.folders.adapter.MusicFileSourceAdapter;
import com.github.anrimian.musicplayer.ui.library.folders.wrappers.HeaderViewWrapper;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatFragment;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.menu.MenuItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.PATH_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.FILE_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_FOR_FOLDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.shareFile;
import static com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils.shareFiles;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

/**
 * Created on 23.10.2017.
 */

public class LibraryFoldersFragment extends MvpAppCompatFragment
        implements LibraryFoldersView, BackButtonListener, FragmentLayerListener {

    @InjectPresenter
    LibraryFoldersPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    View fab;

    @BindView(R.id.header_container)
    View headerContainer;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    @BindView(R.id.content_container)
    View contentContainer;

    private AdvancedToolbar toolbar;
    private ProgressViewWrapper progressViewWrapper;
    private MusicFileSourceAdapter adapter;

    private HeaderViewWrapper headerViewWrapper;

    private final MenuItemWrapper orderMenuItem = new MenuItemWrapper();

    private boolean showQueueActions;

    private DialogFragmentRunner<InputTextDialogFragment> filenameDialogFragmentRunner;

    public static LibraryFoldersFragment newInstance(@Nullable String path) {
        Bundle args = new Bundle();
        args.putString(PATH_ARG, path);
        LibraryFoldersFragment fragment = new LibraryFoldersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressWarnings("ConstantConditions")
    @Nullable
    private String getPath() {
        return getArguments().getString(PATH_ARG);
    }

    @ProvidePresenter
    LibraryFoldersPresenter providePresenter() {
        return Components.getLibraryFolderComponent(getPath()).storageLibraryPresenter();
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
        return inflater.inflate(R.layout.fragment_library_folders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar = requireActivity().findViewById(R.id.toolbar);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainButtonClicked);
        progressViewWrapper.hideAll();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        adapter = new MusicFileSourceAdapter(presenter.getSelectedFiles());
        adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
        adapter.setOnFolderClickListener(presenter::onFolderClicked);
        adapter.setOnFolderMenuClickListener(this::onFolderMenuClicked);
        adapter.setOnCompositionMenuItemClicked(this::onCompositionMenuClicked);
        adapter.setOnLongClickListener(presenter::onItemLongClick);
        recyclerView.setAdapter(adapter);

        headerViewWrapper = new HeaderViewWrapper(headerContainer);
        headerViewWrapper.setOnClickListener(v -> presenter.onBackPathButtonClicked());

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        FragmentManager fm = getChildFragmentManager();

        SelectOrderDialogFragment orderFragment = (SelectOrderDialogFragment) fm
                .findFragmentByTag(ORDER_TAG);
        if (orderFragment != null) {
            orderFragment.setOnCompleteListener(presenter::onOrderSelected);
        }

        ChoosePlayListDialogFragment playListDialog = (ChoosePlayListDialogFragment) fm
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (playListDialog != null) {
            playListDialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        }

        ChoosePlayListDialogFragment folderPlayListDialog = (ChoosePlayListDialogFragment) fm
                .findFragmentByTag(SELECT_PLAYLIST_FOR_FOLDER_TAG);
        if (folderPlayListDialog != null) {
            folderPlayListDialog.setOnCompleteListener(presenter::onPlayListForFolderSelected);
        }

        MenuDialogFragment compositionDialog = (MenuDialogFragment) fm
                .findFragmentByTag(COMPOSITION_ACTION_TAG);
        if (compositionDialog != null) {
            compositionDialog.setOnCompleteListener(this::onCompositionActionSelected);
        }

        filenameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                FILE_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((path, extra) -> {
                    presenter.onNewFolderNameInputed(extra.getString(PATH_ARG), path);
                }));

        if (getPath() != null) {//TODO root path -> not root path change case
            SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
            SlidrPanel.replace(contentContainer,
                    () -> {
                        toolbar.showSelectionMode(0);
                        FragmentNavigation.from(requireFragmentManager()).goBack();
                    },
                    slidrConfig);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void onFragmentMovedOnTop() {
        presenter.onFragmentDisplayed();

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setupSelectionModeMenu(R.menu.library_folders_selection_menu,
                this::onActionModeItemClicked);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
//        menu.clear();//what fix was it?
        inflater.inflate(R.menu.library_files_menu, menu);
        orderMenuItem.setMenuItem(menu, R.id.menu_order);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.menu_order: {
                presenter.onOrderMenuItemClicked();
                return true;
            }
            case R.id.menu_search: {
                presenter.onSearchButtonClicked();
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void updateList(List<FileSource> list) {
        adapter.submitList(list);
    }

    @Override
    public void showBackPathButton(@Nonnull String path) {
        headerViewWrapper.setVisible(true);
        headerViewWrapper.bind(path);
    }

    @Override
    public void hideBackPathButton() {
        headerViewWrapper.setVisible(false);
    }

    @Override
    public void showEmptyList() {
        orderMenuItem.call(item -> item.setVisible(false));
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_on_device_not_found, false);
    }

    @Override
    public void showEmptySearchResult() {
        orderMenuItem.call(item -> item.setVisible(true));
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_and_folders_for_search_not_found, false);
    }

    @Override
    public void showList() {
        orderMenuItem.call(item -> item.setVisible(true));
        fab.setVisibility(View.VISIBLE);
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        orderMenuItem.call(item -> item.setVisible(false));
        progressViewWrapper.showProgress();
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void goBackToParentFolderScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    @Override
    public boolean onBackPressed() {
        if (toolbar.isInActionMode()) {
            presenter.onSelectionModeBackPressed();
            return true;
        }
        if (toolbar.isInSearchMode()) {
            toolbar.setSearchModeEnabled(false);
            return true;
        }
        if (getPath() != null) {
            presenter.onBackPathButtonClicked();
            return true;
        }
        return false;
    }

    @Override
    public void showSearchMode(boolean show) {
        toolbar.setSearchModeEnabled(show);
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
    public void showSelectPlayListForFolderDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListForFolderSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_FOR_FOLDER_TAG);
    }

    @Override
    public void showSelectOrderScreen(Order folderOrder) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(folderOrder);
        fragment.setOnCompleteListener(presenter::onOrderSelected);
        fragment.show(getChildFragmentManager(), ORDER_TAG);
    }

    @Override
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        dialog.show(getChildFragmentManager(), SELECT_PLAYLIST_TAG);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
    }

    @Override
    public void showConfirmDeleteDialog(FolderFileSource folder) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                folder,
                presenter::onDeleteFolderDialogConfirmed);
    }

    @Override
    public void showDeleteCompositionError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeleteCompositionMessage(List<Composition> compositionsToDelete) {
        String text = getDeleteCompleteMessage(requireActivity(), compositionsToDelete);
        MessagesUtils.makeSnackbar(clListContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void sendCompositions(List<String> paths) {
        shareFiles(requireContext(), paths);
    }

    @Override
    public void showReceiveCompositionsForSendError(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer,
                getString(R.string.can_not_receive_file_for_send, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void goToMusicStorageScreen(String path) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(LibraryFoldersFragment.newInstance(path));
    }

    @Override
    public void showCurrentPlayingComposition(Composition composition) {
        adapter.showPlayingComposition(composition);
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
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showQueueActions(boolean show) {
        showQueueActions = show;
    }

    @Override
    public void setDisplayCoversEnabled(boolean isCoversEnabled) {
        adapter.setCoversEnabled(isCoversEnabled);
    }

    @Override
    public void showInputFolderNameDialog(String path) {
        Bundle extra = new Bundle();
        extra.putString(PATH_ARG, path);
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.rename_folder,
                R.string.change,
                R.string.cancel,
                R.string.folder_name,
                formatFileName(path),
                false,
                extra);
        filenameDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showSelectionMode(int count) {
        toolbar.showSelectionMode(count);
    }

    @Override
    public void onItemSelected(FileSource item, int position) {
        adapter.setItemSelected(position);
    }

    @Override
    public void onItemUnselected(FileSource item, int position) {
        adapter.setItemUnselected(position);
    }

    @Override
    public void setItemsSelected(boolean selected) {
        adapter.setItemsSelected(selected);
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

    private void onCompositionMenuClicked(View view, MusicFileSource musicFileSource) {
        Composition composition = musicFileSource.getComposition();

        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.composition_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    presenter.onAddToPlayListButtonClicked(musicFileSource);
                    return true;
                }
                case R.id.menu_edit: {
                    startActivity(CompositionEditorActivity.newIntent(requireContext(), composition.getId()));
                    return true;
                }
                case R.id.menu_share: {
                    shareFile(requireContext(), composition.getFilePath());
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteCompositionButtonClicked(musicFileSource);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    private boolean onActionModeItemClicked(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_play: {
                presenter.onPlayAllSelectedClicked();
                return true;
            }
            case R.id.menu_select_all: {
                presenter.onSelectAllButtonClicked();
                return true;
            }
            case R.id.menu_play_next: {
                presenter.onPlayNextSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_add_to_queue: {
                presenter.onAddToQueueSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_add_to_playlist: {
                presenter.onAddSelectedCompositionToPlayListClicked();
                return true;
            }
//            case R.id.menu_share: {
//                presenter.onShareSelectedCompositionsClicked();
//                return true;
//            }
//            case R.id.menu_delete: {
//                presenter.onDeleteSelectedCompositionButtonClicked();
//                return true;
//            }
        }
        return false;
    }

    private void onFolderMenuClicked(View view, FolderFileSource folder) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.folder_item_menu);
        Menu menu = popup.getMenu();

        menu.findItem(R.id.menu_play_next).setVisible(showQueueActions);
        menu.findItem(R.id.menu_add_to_queue).setVisible(showQueueActions);

        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_play_next: {
                    presenter.onPlayNextFolderClicked(folder);
                    return true;
                }
                case R.id.menu_add_to_queue: {
                    presenter.onAddToQueueFolderClicked(folder);
                    return true;
                }
                case R.id.menu_add_to_playlist: {
                    presenter.onAddFolderToPlayListButtonClicked(folder.getFullPath());
                    return true;
                }
                case R.id.menu_rename_folder: {
                    presenter.onRenameFolderClicked(folder.getFullPath());
                    return true;
                }
                case R.id.menu_share: {
                    presenter.onShareFolderClicked(folder);
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteFolderButtonClicked(folder);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }
}
