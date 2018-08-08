package com.github.anrimian.simplemusicplayer.ui.library.folders;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.transition.Slide;
import android.support.transition.Transition;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Order;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.common.order.SelectOrderDialogFragment;
import com.github.anrimian.simplemusicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.simplemusicplayer.ui.library.LibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.library.folders.adapter.MusicFileSourceAdapter;
import com.github.anrimian.simplemusicplayer.ui.library.folders.wrappers.HeaderViewWrapper;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.simplemusicplayer.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.simplemusicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.simplemusicplayer.constants.Arguments.PATH;
import static com.github.anrimian.simplemusicplayer.ui.common.format.FormatUtils.formatCompositionName;

/**
 * Created on 23.10.2017.
 */

public class LibraryFoldersFragment extends LibraryFragment implements LibraryFoldersView, BackButtonListener {

    @InjectPresenter
    LibraryFoldersPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.header_container)
    View headerContainer;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private ProgressViewWrapper progressViewWrapper;
    private MusicFileSourceAdapter adapter;
    private HeaderViewWrapper headerViewWrapper;

    public static LibraryFoldersFragment newInstance(@Nullable String path) {
        Bundle args = new Bundle();
        args.putString(PATH, path);
        LibraryFoldersFragment fragment = new LibraryFoldersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private String getPath() {
        return getArguments().getString(PATH);
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
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        return inflater.inflate(R.layout.fragment_library_folders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.files);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.setTryAgainButtonOnClickListener(v -> presenter.onTryAgainButtonClicked());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

//        headerView = View.inflate(getContext(), R.layout.partial_storage_header, null);
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
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.storage_files_menu, menu);
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
            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void bindList(List<FileSource> musicList) {
            adapter = new MusicFileSourceAdapter(musicList);
//        adapter.addHeader(headerView);
        adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
        adapter.setOnFolderClickListener(this::goToMusicStorageScreen);
        adapter.setOnDeleteCompositionClickListener(presenter::onDeleteCompositionButtonClicked);
        adapter.setOnAddToPlaylistClickListener(presenter::onAddToPlayListButtonClicked);
        recyclerView.setAdapter(adapter);
        startPostponedEnterTransition();
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
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(R.string.compositions_on_device_not_found, false);
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
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);//TODO add default handler
    }

    @Override
    public void goBackToMusicStorageScreen(String path) {
        FragmentManager fragmentManager = getFragmentManager();
        //noinspection ConstantConditions
        if (fragmentManager.getBackStackEntryCount() > 0) {
            headerViewWrapper.restoreTransitionInfo();
            fragmentManager.popBackStack();
        } else {
            fragmentManager.beginTransaction()
                    .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                    .replace(R.id.drawer_fragment_container, LibraryFoldersFragment.newInstance(path))
                    .commit();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (getPath() != null) {
            presenter.onBackPathButtonClicked();
            return true;
        }
        return false;
    }

    @Override
    public void updateList(List<FileSource> oldList, List<FileSource> newList) {
        Parcelable recyclerViewState = recyclerView.getLayoutManager().onSaveInstanceState();
        adapter.updateList(oldList, newList);
        recyclerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);
    }

    @Override
    public void showAddingToPlayListError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.add_to_playlist_error_template, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showAddingToPlayListComplete(PlayList playList, Composition composition) {
        String text = getString(R.string.add_to_playlist_success_template,
                formatCompositionName(composition),
                playList.getName());
        Snackbar.make(clListContainer, text, Snackbar.LENGTH_SHORT).show();
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
        dialog.show(getChildFragmentManager(), null);
    }

    private void goToMusicStorageScreen(String path, View... sharedViews) {
        LibraryFoldersFragment fragment = LibraryFoldersFragment.newInstance(path);
        headerViewWrapper.clearTransitionInfo();
        Transition transition = new Slide();
        transition.setDuration(1000);
        fragment.setSharedElementEnterTransition(transition);
        fragment.setSharedElementReturnTransition(transition);
        //noinspection ConstantConditions
        FragmentTransaction transaction = getFragmentManager().beginTransaction();

//        transaction.addSharedElement(recyclerView, ViewCompat.getTransitionName(recyclerView));
//        for (View sharedView: sharedViews) {
//            transaction.addSharedElement(sharedView, ViewCompat.getTransitionName(sharedView));
//        }
        transaction.replace(R.id.drawer_fragment_container, fragment, path)
                .addToBackStack(path)
                .commit();
    }
}
