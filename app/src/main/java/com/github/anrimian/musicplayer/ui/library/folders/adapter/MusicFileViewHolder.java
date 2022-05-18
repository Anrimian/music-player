package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemStorageMusicBinding;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.domain.models.folders.CompositionFileSource;
import com.github.anrimian.musicplayer.domain.models.folders.FileSource;
import com.github.anrimian.musicplayer.domain.utils.functions.Callback;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

public class MusicFileViewHolder extends FileViewHolder implements SwipeListener {

    private final CompositionItemWrapper compositionItemWrapper;

    private CompositionFileSource fileSource;

    private boolean selected = false;
    private boolean selectedToMove = false;
    private boolean isCurrent = false;

    public MusicFileViewHolder(ViewGroup parent,
                               OnPositionItemClickListener<CompositionFileSource> onCompositionClickListener,
                               OnPositionItemClickListener<FileSource> onLongClickListener,
                               Callback<Composition> iconClickListener,
                               OnPositionItemClickListener<CompositionFileSource> menuClickListener) {
        super(parent, R.layout.item_storage_music);
        ItemStorageMusicBinding binding = ItemStorageMusicBinding.bind(itemView);

        compositionItemWrapper = new CompositionItemWrapper(itemView,
                iconClickListener,
                composition -> onCompositionClickListener.onItemClick(getBindingAdapterPosition(), fileSource)
        );
        binding.btnActionsMenu.setOnClickListener(v ->
                menuClickListener.onItemClick(getBindingAdapterPosition(), fileSource)
        );

        if (onLongClickListener != null) {
            binding.clickableItem.setOnLongClickListener(v -> {
                if (selected) {
                    return false;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getBindingAdapterPosition(), fileSource);
                return true;
            });
        }
    }

    public void bind(@Nonnull CompositionFileSource fileSource, boolean isCoversEnabled) {
        this.fileSource = fileSource;
        compositionItemWrapper.bind(fileSource.getComposition(), isCoversEnabled);
    }

    public void update(CompositionFileSource fileSource, List<Object> payloads) {
        this.fileSource = fileSource;
        compositionItemWrapper.update(fileSource.getComposition(), payloads);
        for (Object payload: payloads) {
            if (payload == ITEM_SELECTED) {
                setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                setSelected(false);
                return;
            }
        }
    }

    @Override
    public void release() {
        compositionItemWrapper.release();
    }

    public void setCoversVisible(boolean isCoversEnabled) {
        compositionItemWrapper.showCompositionImage(isCoversEnabled);
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            int unselectedColor = (!selected && isCurrent)? getPlaySelectionColor(): Color.TRANSPARENT;
            int selectedColor = getSelectionColor();
            int endColor = selected ? selectedColor : unselectedColor;
            compositionItemWrapper.showStateColor(endColor, true);
        }
    }

    @Override
    public void setSelectedToMove(boolean selected) {
        if (this.selectedToMove != selected) {
            this.selectedToMove = selected;
            int unselectedColor = Color.TRANSPARENT;
            int selectedColor = getMoveSelectionColor();
            int endColor = selected ? selectedColor : unselectedColor;
            compositionItemWrapper.showStateColor(endColor, true);
        }
    }

    @Override
    public FileSource getFileSource() {
        return fileSource;
    }

    @Override
    public void onSwipeStateChanged(float swipeOffset) {
        compositionItemWrapper.showAsSwipingItem(swipeOffset);
    }

    public void showCurrentComposition(@Nullable CurrentComposition currentComposition,
                                       boolean animate) {
        boolean isCurrent = false;
        boolean isPlaying = false;
        if (currentComposition != null) {
            isCurrent = fileSource.getComposition().equals(currentComposition.getComposition());
            isPlaying = isCurrent && currentComposition.isPlaying();
        }
        showAsCurrentComposition(isCurrent);
        compositionItemWrapper.showAsPlaying(isPlaying, animate);
    }

    private void showAsCurrentComposition(boolean isCurrent) {
        if (this.isCurrent != isCurrent) {
            this.isCurrent = isCurrent;
            if (!selected) {
                compositionItemWrapper.showAsCurrentComposition(isCurrent);
            }
        }
    }

    private void selectImmediate() {
        compositionItemWrapper.showStateColor(getSelectionColor(), false);
        selected = true;
    }

    @ColorInt
    private int getPlaySelectionColor() {
        return getPlayingCompositionColor(getContext(), 20);
    }
}
