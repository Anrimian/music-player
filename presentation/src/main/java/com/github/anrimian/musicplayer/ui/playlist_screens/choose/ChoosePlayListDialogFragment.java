package com.github.anrimian.musicplayer.ui.playlist_screens.choose;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.domain.models.utils.PlayListHelper;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;
import com.github.anrimian.musicplayer.ui.utils.moxy.MvpBottomSheetDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.utils.AndroidUtils.getFloat;

public class ChoosePlayListDialogFragment extends MvpBottomSheetDialogFragment
        implements ChoosePlayListView {

    @InjectPresenter
    ChoosePlayListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.iv_create_playlist)
    ImageView ivCreatePlaylist;

    @Nullable
    private OnCompleteListener<PlayList> onCompleteListener;

    private PlayListsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;

    @ProvidePresenter
    ChoosePlayListPresenter providePresenter() {
        return Components.getAppComponent().choosePlayListPresenter();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View view = View.inflate(getContext(), R.layout.dialog_select_play_list, null);
        dialog.setContentView(view);

        DisplayMetrics displayMetrics = requireActivity().getResources().getDisplayMetrics();

        int height = displayMetrics.heightPixels;

        float heightPercent = getFloat(getResources(), R.dimen.choose_playlist_dialog_height);
        int minHeight = (int) (height * heightPercent);
        view.setMinimumHeight(minHeight);

        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from((View) view.getParent());
        bottomSheetBehavior.setPeekHeight(minHeight);

        ButterKnife.bind(this, view);

        progressViewWrapper = new ProgressViewWrapper(view);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        ivCreatePlaylist.setOnClickListener(v -> onCreatePlayListButtonClicked());
    }

    @Override
    public void showEmptyList() {
        progressViewWrapper.hideAll();
        progressViewWrapper.showMessage(R.string.play_lists_on_device_not_found, false);
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
    public void bindList(List<PlayList> playLists) {
        adapter = new PlayListsAdapter(playLists);
        adapter.setOnItemClickListener(this::onPlayListSelected);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void updateList(List<PlayList> oldList, List<PlayList> newList) {
        DiffUtilHelper.update(oldList, newList, PlayListHelper::areSourcesTheSame, recyclerView);
    }

    public void setOnCompleteListener(@Nullable OnCompleteListener<PlayList> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void onPlayListSelected(PlayList playList) {
        if (onCompleteListener != null) {
            onCompleteListener.onComplete(playList);
        }
        dismiss();
    }

    private void onCreatePlayListButtonClicked() {
        CreatePlayListDialogFragment fragment = new CreatePlayListDialogFragment();
        fragment.show(getChildFragmentManager(), null);
    }
}
