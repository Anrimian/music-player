package com.github.anrimian.musicplayer.ui.library.compositions;

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
import com.github.anrimian.musicplayer.domain.models.composition.order.Order;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.order.SelectOrderDialogFragment;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.LibraryFragment;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter;
import com.github.anrimian.musicplayer.ui.playlist_screens.choose.ChoosePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.views.menu.MenuItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.DefferedObject;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Tags.COMPOSITION_ACTION_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.ORDER_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.SELECT_PLAYLIST_TAG;
import static com.github.anrimian.musicplayer.domain.models.composition.CompositionModelHelper.formatCompositionName;
import static com.github.anrimian.musicplayer.ui.common.DialogUtils.shareFile;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getAddToPlayListCompleteMessage;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.getDeleteCompleteMessage;

public class LibraryCompositionsFragment extends LibraryFragment implements
        LibraryCompositionsView, BackButtonListener, FragmentLayerListener {

    @InjectPresenter
    LibraryCompositionsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    View fab;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private CompositionsAdapter adapter;
    private final DefferedObject<CompositionsAdapter> adapterWrapper = new DefferedObject<>();
    private ProgressViewWrapper progressViewWrapper;

    private final MenuItemWrapper orderMenuItem = new MenuItemWrapper();
    private final MenuItemWrapper searchMenuItem = new MenuItemWrapper();

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

        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTextChangeListener(presenter::onSearchTextChanged);
        toolbar.setTextConfirmListener(presenter::onSearchTextChanged);
        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu,
                this::onActionModeItemClicked);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

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

        MenuDialogFragment compositionDialog = (MenuDialogFragment) getChildFragmentManager()
                .findFragmentByTag(COMPOSITION_ACTION_TAG);
        if (compositionDialog != null) {
            compositionDialog.setOnCompleteListener(this::onCompositionActionSelected);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.onStart();
    }

    @Override
    public void onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop();
        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.compositions);
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onStop();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.library_compositions_menu, menu);
        searchMenuItem.setMenuItem(menu, R.id.menu_search);
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
                toolbar.setSearchModeEnabled(true);
                return true;
            }
            default: return super.onOptionsItemSelected(item);
        }
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
        return false;
    }

    @Override
    public void showEmptyList() {
        searchMenuItem.call(item -> item.setVisible(false));
        orderMenuItem.call(item -> item.setVisible(false));
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_on_device_not_found);
    }

    @Override
    public void showEmptySearchResult() {
        searchMenuItem.call(item -> item.setVisible(true));
        orderMenuItem.call(item -> item.setVisible(true));
        fab.setVisibility(View.GONE);
        progressViewWrapper.showMessage(R.string.compositions_for_search_not_found);
    }

    @Override
    public void showList() {
        searchMenuItem.call(item -> item.setVisible(true));
        orderMenuItem.call(item -> item.setVisible(true));
        fab.setVisibility(View.VISIBLE);
        progressViewWrapper.hideAll();
    }

    @Override
    public void showLoading() {
        searchMenuItem.call(item -> item.setVisible(false));
        orderMenuItem.call(item -> item.setVisible(false));
        progressViewWrapper.showProgress();
    }

    @Override
    public void showLoadingError(ErrorCommand errorCommand) {
        searchMenuItem.call(item -> item.setVisible(false));
        orderMenuItem.call(item -> item.setVisible(false));
        progressViewWrapper.showMessage(errorCommand.getMessage(), true);
    }

    @Override
    public void updateList(ListUpdate<Composition> update,
                           HashSet<Composition> selectedCompositionsMap) {
        List<Composition> list = update.getNewList();
        if (adapter == null) {
            adapter = new CompositionsAdapter(list, selectedCompositionsMap);
            adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
            adapter.setOnMenuItemClickListener(this::onCompositionMenuClicked);
            adapter.setOnLongClickListener(presenter::onCompositionLongClick);
            adapterWrapper.setObject(adapter);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(list);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    @Override
    public void onCompositionSelected(Composition composition, int position) {
        adapter.setItemSelected(position);
    }

    @Override
    public void onCompositionUnselected(Composition composition, int position) {
        adapter.setItemUnselected(position);
    }

    @Override
    public void setItemsSelected(boolean selected) {
        adapter.setItemsSelected(selected);
    }

    @Override
    public void showSelectionMode(int count) {
        toolbar.showSelectionMode(count);
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
        ChoosePlayListDialogFragment dialog = toolbar.isInActionMode()?
                ChoosePlayListDialogFragment.newInstance(R.attr.actionModeStatusBarColor)
                : new ChoosePlayListDialogFragment();
        dialog.setOnCompleteListener(presenter::onPlayListToAddingSelected);
        dialog.show(getChildFragmentManager(), null);
    }

    @Override
    public void showSelectOrderScreen(Order order) {
        SelectOrderDialogFragment fragment = SelectOrderDialogFragment.newInstance(order);
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

    @Override
    public void shareCompositions(Collection<Composition> selectedCompositions) {
        DialogUtils.shareCompositions(requireContext(), selectedCompositions);
    }

    @Override
    public void showCurrentPlayingComposition(Composition composition) {
        adapter.showPlayingComposition(composition);
    }

    @Override
    public void setDisplayCoversEnabled(boolean isCoversEnabled) {
        adapterWrapper.call(adapter -> adapter.setCoversEnabled(isCoversEnabled));
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
        Snackbar.make(clListContainer, errorCommand.getMessage(), Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void showQueueActions(boolean show) {
        toolbar.editActionMenu(menu -> {
           menu.findItem(R.id.menu_play_next).setVisible(show);
           menu.findItem(R.id.menu_add_to_queue).setVisible(show);
        });
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
            case R.id.menu_share: {
                presenter.onShareSelectedCompositionsClicked();
                return true;
            }
            case R.id.menu_delete: {
                presenter.onDeleteSelectedCompositionButtonClicked();
                return true;
            }
        }
        return false;
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
                    shareFile(requireContext(), composition.getFilePath());
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
}
