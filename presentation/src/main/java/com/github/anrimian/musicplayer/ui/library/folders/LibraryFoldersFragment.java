package com.github.anrimian.musicplayer.ui.library.folders;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.folders.adapter.MusicFileSourceAdapter;
import com.github.anrimian.musicplayer.ui.library.folders.wrappers.HeaderViewWrapper;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.PATH_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_FOR_FOLDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

/**
 * Created on 23.10.2017.
 */

public class LibraryFoldersFragment extends MvpAppCompatFragment implements LibraryFoldersView, BackButtonListener {

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
        return Components.getLibraryFilesComponent(getPath()).storageLibraryPresenter();
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
        progressViewWrapper.setTryAgainButtonOnClickListener(v -> presenter.onTryAgainButtonClicked());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        headerViewWrapper = new HeaderViewWrapper(headerContainer);
        headerViewWrapper.setOnClickListener(v -> presenter.onBackPathButtonClicked());

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());

        SelectOrderDialogFragment fragment = (SelectOrderDialogFragment) getChildFragmentManager()
                .findFragmentByTag(ORDER_TAG);
        if (fragment != null) {
            fragment.setOnCompleteListener(presenter::onOrderSelected);
        }

        ChoosePlayListDialogFragment playListDialog = (ChoosePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(SELECT_PLAYLIST_TAG);
        if (playListDialog != null) {
            playListDialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        }

        ChoosePlayListDialogFragment folderPlayListDialog = (ChoosePlayListDialogFragment) getChildFragmentManager()
                .findFragmentByTag(SELECT_PLAYLIST_FOR_FOLDER_TAG);
        if (folderPlayListDialog != null) {
            folderPlayListDialog.setOnCompleteListener(presenter::onPlayListForFolderSelected);
        }

        if (getPath() != null) {//TODO root path -> not root path change case
            SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
            SlidrPanel.replace(contentContainer,
                    () -> FragmentNavigation.from(requireFragmentManager()).goBack(),
                    slidrConfig);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.library_files_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
    public void updateList(ListUpdate<FileSource> update) {
        List<FileSource> musicList = update.getNewList();
        if (adapter == null) {
            adapter = new MusicFileSourceAdapter(musicList);
            adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
            adapter.setOnFolderClickListener(this::goToMusicStorageScreen);
            adapter.setOnDeleteFolderClickListener(presenter::onDeleteFolderButtonClicked);
            adapter.setOnCompositionMenuItemClicked(this::onCompositionMenuClicked);
            adapter.setOnAddFolderToPlaylistClickListener(presenter::onAddFolderToPlayListButtonClicked);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(musicList);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    @Override
    public void showBackPathButton(@NonNull String path) {
        headerViewWrapper.setVisible(true);
        headerViewWrapper.bind(path);
    }

    @Override
    public void hideBackPathButton() {
        headerViewWrapper.setVisible(false);
    }

    @Override
    public void showEmptyList() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_on_device_not_found, false);
    }

    @Override
    public void showEmptySearchResult() {
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_and_folders_for_search_not_found, false);
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
    public void showError(ErrorCommand errorCommand) {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void goBackToMusicStorageScreen(String path) {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    @Override
    public boolean onBackPressed() {
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

    private void onCompositionMenuClicked(View view, Composition composition) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.composition_item_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_add_to_playlist: {
                    presenter.onAddToPlayListButtonClicked(composition);
                    return true;
                }
                case R.id.menu_share: {
//                    presenter.onDeleteCompositionButtonClicked(composition);
                    return true;
                }
                case R.id.menu_delete: {
                    presenter.onDeleteCompositionButtonClicked(composition);
                    return true;
                }
            }
            return false;
        });
        popup.show();
    }

    private void goToMusicStorageScreen(String path) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(() -> LibraryFoldersFragment.newInstance(path));
    }
}
