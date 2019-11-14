package com.github.anrimian.musicplayer.ui.library.genres.items;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.genres.Genre;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.compositions.adapter.CompositionsAdapter;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import moxy.MvpAppCompatFragment;
import moxy.presenter.InjectPresenter;
import moxy.presenter.ProvidePresenter;

import static com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG;

public class GenreItemsFragment extends MvpAppCompatFragment implements
        GenreItemsView, FragmentLayerListener {

    @InjectPresenter
    GenreItemsPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    private AdvancedToolbar toolbar;
    private CompositionsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    public static GenreItemsFragment newInstance(long genreId) {
        Bundle args = new Bundle();
        args.putLong(ID_ARG, genreId);
        GenreItemsFragment fragment = new GenreItemsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @ProvidePresenter
    GenreItemsPresenter providePresenter() {
        return Components.genreItemsComponent(getGenreId()).genreItemsPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_base_fab_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitleClickListener(null);
//        toolbar.setTextChangeListener(presenter::onSearchTextChanged);
//        toolbar.setTextConfirmListener(presenter::onSearchTextChanged);
//        toolbar.setupSelectionModeMenu(R.menu.library_compositions_selection_menu,
//                this::onActionModeItemClicked);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.onTryAgainClick(presenter::onTryAgainLoadCompositionsClicked);
        progressViewWrapper.hideAll();

        adapter = new CompositionsAdapter(recyclerView, new HashSet<>());
        recyclerView.setAdapter(adapter);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        SlidrConfig slidrConfig = new SlidrConfig.Builder().position(SlidrPosition.LEFT).build();
        SlidrPanel.replace(clListContainer, slidrConfig, () ->
                        FragmentNavigation.from(requireFragmentManager()).goBack(0),
                toolbar::onStackFragmentSlided);
    }

    @Override
    public void onFragmentMovedOnTop() {
//        super.onFragmentMovedOnTop();
        presenter.onFragmentMovedToTop();
    }

    @Override
    public void showGenreInfo(Genre genre) {
        toolbar.setTitle(genre.getName());
        toolbar.setSubtitle(getResources().getQuantityString(
                R.plurals.compositions_count,
                genre.getCompositionsCount(),
                genre.getCompositionsCount()));
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.showMessage(R.string.no_items_in_genre);
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
    public void submitList(List<Composition> genres) {
        adapter.submitList(genres);
    }

    @Override
    public void closeScreen() {
        FragmentNavigation.from(requireFragmentManager()).goBack();
    }

    private long getGenreId() {
        return Objects.requireNonNull(getArguments()).getLong(ID_ARG);
    }
}
