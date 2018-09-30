package com.github.anrimian.musicplayer.ui.library.compositions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

public class LibraryCompositionsFragment extends LibraryFragment implements LibraryCompositionsView {

    @InjectPresenter
    LibraryCompositionsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private CompositionsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    @ProvidePresenter
    LibraryCompositionsPresenter providePresenter() {
        return Components.getLibraryCompositionsComponent().libraryCompositionsPresenter();
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
        return inflater.inflate(R.layout.fragment_library_compositions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.compositions);

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

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
            default: return super.onOptionsItemSelected(item);
        }
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
    public void bindList(List<Composition> compositions) {
        adapter = new CompositionsAdapter(compositions);
        adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
        adapter.setOnDeleteCompositionClickListener(presenter::onDeleteCompositionButtonClicked);
        adapter.setOnAddToPlaylistClickListener(presenter::onAddToPlayListButtonClicked);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateList(List<Composition> oldList, List<Composition> newList) {
        DiffUtilHelper.update(oldList, newList, CompositionHelper::areSourcesTheSame, recyclerView);
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
    public void showSelectPlayListDialog() {
        ChoosePlayListDialogFragment dialog = new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        dialog.show(getChildFragmentManager(), null);
    }

    @Override
    public void showSelectOrderScreen(Order folderOrder) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(folderOrder);
        fragment.setOnCompleteListener(presenter::onOrderSelected);
        fragment.show(getChildFragmentManager(), ORDER_TAG);
    }

    @Override
    public void showConfirmDeleteDialog(List<Composition> compositionsToDelete) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                compositionsToDelete,
                presenter::onDeleteCompositionsDialogConfirmed);
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
}
