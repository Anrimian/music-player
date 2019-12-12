package com.github.anrimian.musicplayer.ui.library.albums.list;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.albums.Album;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils;
import com.github.anrimian.musicplayer.ui.common.serealization.AlbumSerializer;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.editor.album.AlbumEditorActivity;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.albums.items.AlbumItemsFragment;
import com.github.anrimian.musicplayer.ui.library.albums.list.adapter.AlbumsAdapter;
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_MENU_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ALBUM_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.PROGRESS_DIALOG_TAG;

public class AlbumsListFragment extends LibraryFragment implements
        AlbumsListView, FragmentLayerListener {

    @InjectPresenter
    AlbumsListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private AlbumsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    private DialogFragmentRunner<MenuDialogFragment> albumMenuDialogRunner;
    private DialogFragmentRunner<InputTextDialogFragment> editAlbumNameDialogRunner;

    @ProvidePresenter
    AlbumsListPresenter providePresenter() {
        return Components.albumsComponent().albumsListPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_albums, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar = requireActivity().findViewById(R.id.toolbar);
//        toolbar.setTextChangeListener(presenter::onSearchTextChanged);
//        toolbar.setTextConfirmListener(presenter::onSearchTextChanged);
//        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu,
//                this::onActionModeItemClicked);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        adapter = new AlbumsAdapter(recyclerView, this::goToAlbumScreen, this::onAlbumLongClick);
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        FragmentManager fm = getChildFragmentManager();
        albumMenuDialogRunner = new DialogFragmentRunner<>(fm,
                ALBUM_MENU_TAG,
                fragment -> fragment.setComplexCompleteListener(this::onAlbumMenuClicked)
        );
        editAlbumNameDialogRunner = new DialogFragmentRunner<>(fm,
                ALBUM_NAME_TAG,
                fragment -> fragment.setComplexCompleteListener((name, extra) -> {
                    presenter.onNewAlbumNameEntered(name, extra.getLong(ID_ARG));
                })
        );
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.albums);
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.showMessage(R.string.no_albums_in_library);
    }

    @Override
    public void showEmptySearchResult() {
        progressViewWrapper.showMessage(R.string.compositions_for_search_not_found);
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
    public void showLoadingError(ErrorCommand errorCommand) {
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void submitList(List<Album> albums) {
        adapter.submitList(albums);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        MessagesUtils.makeSnackbar(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showRenameProgress() {
        ProgressDialogFragment fragment = ProgressDialogFragment.newInstance(
                getString(R.string.rename_progress)
        );
        fragment.show(getChildFragmentManager(), PROGRESS_DIALOG_TAG);
    }

    @Override
    public void hideRenameProgress() {
        ProgressDialogFragment fragment = (ProgressDialogFragment) getChildFragmentManager()
                .findFragmentByTag(PROGRESS_DIALOG_TAG);
        if (fragment != null) {
            fragment.dismissAllowingStateLoss();
        }
    }

    private void onAlbumMenuClicked(MenuItem menuItem, Bundle extra) {
        Album album = AlbumSerializer.deserialize(extra);
        switch (menuItem.getItemId()) {
            case R.id.menu_edit: {
                startActivity(AlbumEditorActivity.newIntent(requireContext(), album.getId()));
                break;
            }
        }
    }

    private void showEditAlbumNameDialog(Album album) {
        Bundle bundle = new Bundle();
        bundle.putLong(ID_ARG, album.getId());
        InputTextDialogFragment fragment = new InputTextDialogFragment.Builder(R.string.change_name,
                R.string.change,
                R.string.cancel,
                R.string.name,
                album.getName())
                .canBeEmpty(false)
                .extra(bundle)
                .build();
        editAlbumNameDialogRunner.show(fragment);
    }

    private void goToAlbumScreen(Album album) {
        FragmentNavigation.from(requireFragmentManager())
                .addNewFragment(AlbumItemsFragment.newInstance(album.getId()));
    }

    private void onAlbumLongClick(Album album) {
        Bundle extra = AlbumSerializer.serialize(album);
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(
                R.menu.album_menu,
                album.getName(),
                extra
        );
        albumMenuDialogRunner.show(fragment);
    }
}
