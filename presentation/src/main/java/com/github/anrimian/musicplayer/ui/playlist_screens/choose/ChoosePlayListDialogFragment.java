package com.github.anrimian.musicplayer.ui.playlist_screens.choose;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList;
import com.github.anrimian.musicplayer.ui.common.DialogUtils;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment;
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter;
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener;
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpBottomSheetDialogFragment;
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.MotionLayoutDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.StatusBarColorDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextColorDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextSizeDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.delegate.VisibilityDelegate;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.DiffUtilHelper;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.calculator.ListUpdate;
import com.github.anrimian.musicplayer.ui.utils.wrappers.ProgressViewWrapper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.motion.widget.MotionLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static androidx.core.content.ContextCompat.getColor;
import static com.github.anrimian.musicplayer.Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.PLAY_LIST_MENU;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getContentView;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getFloat;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getStatusBarHeight;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateVisibility;

public class ChoosePlayListDialogFragment extends MvpBottomSheetDialogFragment
        implements ChoosePlayListView {

    @InjectPresenter
    ChoosePlayListPresenter presenter;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.iv_create_playlist)
    ImageView ivCreatePlaylist;

    @BindView(R.id.iv_close)
    ImageView ivClose;

    @BindView(R.id.title_shadow)
    View titleShadow;

    @BindView(R.id.motion_layout)
    MotionLayout motionLayout;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.list_container)
    CoordinatorLayout clListContainer;

    @Nullable
    private OnCompleteListener<PlayList> onCompleteListener;

    private PlayListsAdapter adapter;
    private ProgressViewWrapper progressViewWrapper;
    private SlideDelegate slideDelegate;

    public static ChoosePlayListDialogFragment newInstance(@AttrRes int statusBarColorAttr) {
        Bundle args = new Bundle();
        args.putInt(STATUS_BAR_COLOR_ATTR_ARG, statusBarColorAttr);
        ChoosePlayListDialogFragment fragment = new ChoosePlayListDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

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
        progressViewWrapper.hideAll();

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        titleShadow.setVisibility(
                recyclerView.computeVerticalScrollOffset() > 0? VISIBLE: INVISIBLE
        );
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                animateVisibility(
                        titleShadow,
                        recyclerView.computeVerticalScrollOffset() > 0? VISIBLE: INVISIBLE
                );
            }
        });

        bottomSheetBehavior.setBottomSheetCallback(new SimpleBottomSheetCallback(newState -> {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }, presenter::onBottomSheetSlided));

        slideDelegate = buildSlideDelegate();

        ivClose.setOnClickListener(v -> dismiss());
        ivClose.setVisibility(INVISIBLE);//start state
        ivCreatePlaylist.setOnClickListener(v -> onCreatePlayListButtonClicked());

        MenuDialogFragment fragment = (MenuDialogFragment) getChildFragmentManager()
                .findFragmentByTag(PLAY_LIST_MENU);
        if (fragment != null) {
            fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        slideDelegate.onSlide(0f);
    }

    @Override
    public void showBottomSheetSlided(float slideOffset) {
        recyclerView.post(() -> {
            View contentView = getContentView(getActivity());
            if (contentView == null) {
                return;
            }
            float usableSlideOffset = slideOffset;
            int activityHeight = contentView.getHeight() - getStatusBarHeight(getContext());
            int viewHeight = clListContainer.getHeight();
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0;
            }
            slideDelegate.onSlide(usableSlideOffset);
        });
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
    public void updateList(ListUpdate<PlayList> update) {
        List<PlayList> list = update.getNewList();
        if (adapter == null) {
            adapter = new PlayListsAdapter(list);
            adapter.setOnItemClickListener(this::onPlayListSelected);
            adapter.setOnItemLongClickListener(presenter::onPlayListLongClick);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.setItems(list);
            DiffUtilHelper.update(update.getDiffResult(), recyclerView);
        }
    }

    @Override
    public void showPlayListMenu(PlayList playList) {
        MenuDialogFragment fragment = MenuDialogFragment.newInstance(R.menu.play_list_menu, playList.getName());
        fragment.setOnCompleteListener(this::onPlayListMenuItemSelected);
        fragment.show(getChildFragmentManager(), PLAY_LIST_MENU);
    }

    @Override
    public void showConfirmDeletePlayListDialog(PlayList playList) {
        DialogUtils.showConfirmDeleteDialog(requireContext(),
                playList,
                presenter::onDeletePlayListDialogConfirmed);
    }

    @Override
    public void showPlayListDeleteSuccess(PlayList playList) {
        Snackbar.make(clListContainer,
                getString(R.string.play_list_deleted, playList.getName()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    @Override
    public void showDeletePlayListError(ErrorCommand errorCommand) {
        Snackbar.make(clListContainer,
                getString(R.string.play_list_delete_error, errorCommand.getMessage()),
                Snackbar.LENGTH_SHORT)
                .show();
    }

    private void onPlayListMenuItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_delete_play_list: {
                presenter.onDeletePlayListButtonClicked();
                break;
            }
        }
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

    private SlideDelegate buildSlideDelegate() {
        SlideDelegate boundDelegate = new DelegateManager()
                .addDelegate(
                        new BoundValuesDelegate(0.85f, 1f,
                                new VisibilityDelegate(ivClose)
                        )
                )
                .addDelegate(
                        new BoundValuesDelegate(0.7f, 1f,
                                new DelegateManager()
                                        .addDelegate(new MotionLayoutDelegate(motionLayout))
                                        .addDelegate(new TextSizeDelegate(tvTitle,
                                                R.dimen.sheet_dialog_title_collapsed_size,
                                                R.dimen.sheet_dialog_title_expanded_size))
                                        .addDelegate(new TextColorDelegate(tvTitle,
                                                android.R.attr.textColorSecondary,
                                                android.R.attr.textColorPrimary)
                                        )
                        )
                );
        return new DelegateManager()
                .addDelegate(new BoundValuesDelegate(0.008f, 0.95f, boundDelegate))
                .addDelegate(new BoundValuesDelegate(0.85f, 1f,
                                new StatusBarColorDelegate(getActivity().getWindow(),
                                        getColorFromAttr(getContext(), getStatusBarColorAttr()),
                                        getColor(getContext(), R.color.colorPrimaryDarkSecondary))
                        )
                );
    }

    @AttrRes
    private int getStatusBarColorAttr() {
        Bundle args = getArguments();
        if (args != null) {
            int colorAttr = args.getInt(STATUS_BAR_COLOR_ATTR_ARG);
            if (colorAttr != 0) {
                return colorAttr;
            }
        }
        return android.R.attr.statusBarColor;
    }
}
