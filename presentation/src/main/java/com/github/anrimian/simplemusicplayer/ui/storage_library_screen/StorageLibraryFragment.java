package com.github.anrimian.simplemusicplayer.ui.storage_library_screen;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.transition.Slide;
import android.support.transition.Transition;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.storage_library_screen.adapter.MusicFileSourceAdapter;
import com.github.anrimian.simplemusicplayer.ui.storage_library_screen.wrappers.HeaderViewWrapper;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;
import com.github.anrimian.simplemusicplayer.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.simplemusicplayer.constants.Arguments.PATH;

/**
 * Created on 23.10.2017.
 */

public class StorageLibraryFragment extends MvpAppCompatFragment implements StorageLibraryView, BackButtonListener {

    @InjectPresenter
    StorageLibraryPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.header_container)
    View headerContainer;

    private ProgressViewWrapper progressViewWrapper;
    private MusicFileSourceAdapter adapter;
    private HeaderViewWrapper headerViewWrapper;

    public static StorageLibraryFragment newInstance(@Nullable String path) {
        Bundle args = new Bundle();
        args.putString(PATH, path);
        StorageLibraryFragment fragment = new StorageLibraryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    private String getPath() {
        return getArguments().getString(PATH);
    }

    @ProvidePresenter
    StorageLibraryPresenter providePresenter() {
        return Components.getStorageLibraryComponent(getPath()).storageLibraryPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        return inflater.inflate(R.layout.fragment_library_storage, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        getActivity().setTitle(R.string.library);

        progressViewWrapper = new ProgressViewWrapper(view);
        progressViewWrapper.setTryAgainButtonOnClickListener(v -> presenter.onTryAgainButtonClicked());

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

//        headerView = View.inflate(getContext(), R.layout.partial_storage_header, null);
        headerViewWrapper = new HeaderViewWrapper(headerContainer);
        headerViewWrapper.setOnClickListener(v -> presenter.onBackPathButtonClicked());

        fab.setOnClickListener(v -> presenter.onPlayAllButtonClicked());
    }

    @Override
    public void bindList(List<FileSource> musicList) {
            adapter = new MusicFileSourceAdapter(musicList);
//        adapter.addHeader(headerView);
        adapter.setOnCompositionClickListener(presenter::onCompositionClicked);
        adapter.setOnFolderClickListener(this::goToMusicStorageScreen);
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
    public void notifyItemsLoaded(int start, int size) {
        adapter.notifyItemRangeInserted(++start, size);
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
                    .replace(R.id.drawer_fragment_container, StorageLibraryFragment.newInstance(path))
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
    public void updateList(List<FileSource> oldList, List<FileSource> sourceList) {
        adapter.updateList(oldList, sourceList);
    }

    private void goToMusicStorageScreen(String path, View... sharedViews) {
        StorageLibraryFragment fragment = StorageLibraryFragment.newInstance(path);
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
