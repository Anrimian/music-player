package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import static com.github.anrimian.musicplayer.domain.Payloads.FILES_COUNT;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.NAME;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionsCount;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.animateItemDrawableCorners;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateItemDrawableColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.RippleDrawable;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemStorageFolderBinding;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener;

import java.util.List;

import javax.annotation.Nonnull;


/**
 * Created on 31.10.2017.
 */

class FolderViewHolder extends FileViewHolder implements SwipeListener {

    private final ItemStorageFolderBinding viewBinding;

    private FolderFileSource folder;

    private final ItemDrawable backgroundDrawable = new ItemDrawable();
    private final ItemDrawable stateDrawable = new ItemDrawable();
    private final ItemDrawable rippleMaskDrawable = new ItemDrawable();

    private boolean isSelected;
    private boolean isSelectedForMove;
    private boolean isSwiping;

    FolderViewHolder(ViewGroup parent,
                     OnPositionItemClickListener<FolderFileSource> onFolderClickListener,
                     OnViewItemClickListener<FolderFileSource> onMenuClickListener,
                     OnPositionItemClickListener<FileSource> onLongClickListener) {
        super(parent, R.layout.item_storage_folder);
        viewBinding = ItemStorageFolderBinding.bind(itemView);

        if (onFolderClickListener != null) {
            viewBinding.clickableItem.setOnClickListener(v ->
                    onFolderClickListener.onItemClick(getBindingAdapterPosition(), folder)
            );
        }
        if (onLongClickListener != null) {
            onLongClick(viewBinding.clickableItem,  () -> {
                if (isSelected) {
                    return;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getBindingAdapterPosition(), folder);
            });
        }
        viewBinding.btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, folder));

        backgroundDrawable.setColor(getColorFromAttr(getContext(), R.attr.listItemBackground));
        itemView.setBackground(backgroundDrawable);
        stateDrawable.setColor(Color.TRANSPARENT);
        viewBinding.clickableItem.setBackground(stateDrawable);
        viewBinding.clickableItem.setForeground(new RippleDrawable(
                ColorStateList.valueOf(getColorFromAttr(getContext(), android.R.attr.colorControlHighlight)),
                null,
                rippleMaskDrawable));
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.isSelected != selected) {
            this.isSelected = selected;
            updateSelectionState();
        }
    }

    @Override
    public void setSelectedToMove(boolean selected) {
        if (this.isSelectedForMove != selected) {
            this.isSelectedForMove = selected;
            updateSelectionState();
        }
    }

    @Override
    public FileSource getFileSource() {
        return folder;
    }

    @Override
    public void onSwipeStateChanged(float swipeOffset) {
        boolean swiping = swipeOffset > 0.0f;

        if (this.isSwiping != swiping) {
            this.isSwiping = swiping;

            float swipedCorners = getContext().getResources().getDimension(R.dimen.swiped_item_corners);
            float from = swiping? 0: swipedCorners;
            float to = swiping? swipedCorners: 0;
            int duration = getContext().getResources().getInteger(R.integer.swiped_item_animation_time);
            animateItemDrawableCorners(from, to, duration, backgroundDrawable, stateDrawable, rippleMaskDrawable);
        }
    }

    void bind(@Nonnull FolderFileSource folderFileSource) {
        this.folder = folderFileSource;
        showFolderName();
        showFilesCount();
    }

    public void update(FolderFileSource folderFileSource, List<Object> payloads) {
        this.folder = folderFileSource;
        bind(folderFileSource);
        for (Object payload: payloads) {
            if (payload instanceof List) {
                //noinspection SingleStatementInBlock,unchecked
                update(folderFileSource, (List<Object>) payload);
            }
            if (payload == ITEM_SELECTED) {
                setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                setSelected(false);
                return;
            }
            if (payload == NAME) {
                showFolderName();
            }
            if (payload == FILES_COUNT) {
                showFilesCount();
            }
        }
    }

    private void showFilesCount() {
        String text = formatCompositionsCount(getContext(), folder.getFilesCount());
        viewBinding.tvCompositionsCount.setText(text);
    }

    private void showFolderName() {
        viewBinding.tvFolderName.setText(folder.getName());
    }

    private void updateSelectionState() {
        int stateColor = isSelected? getSelectionColor()
                : (isSelectedForMove? getMoveSelectionColor() : Color.TRANSPARENT);
        animateItemDrawableColor(stateDrawable, stateColor);
    }

    private void selectImmediate() {
        stateDrawable.setColor(getSelectionColor());
        isSelected = true;
    }
}
