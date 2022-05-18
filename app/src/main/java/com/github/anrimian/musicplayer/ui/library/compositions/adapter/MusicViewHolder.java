package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;

import android.graphics.Color;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.ItemStorageMusicBinding;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.short_swipe.SwipeListener;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Created on 31.10.2017.
 */

public class MusicViewHolder extends SelectableViewHolder implements SwipeListener {

    private final CompositionItemWrapper compositionItemWrapper;

    private Composition composition;

    private boolean selected = false;
    private boolean isCurrent = false;

    public MusicViewHolder(ViewGroup parent,
                           OnPositionItemClickListener<Composition> onCompositionClickListener,
                           OnPositionItemClickListener<Composition> onLongClickListener,
                           OnPositionItemClickListener<Composition> iconClickListener,
                           OnPositionItemClickListener<Composition> menuClickListener) {
        super(parent, R.layout.item_storage_music);
        ItemStorageMusicBinding binding = ItemStorageMusicBinding.bind(itemView);

        compositionItemWrapper = new CompositionItemWrapper(itemView,
                o -> iconClickListener.onItemClick(getBindingAdapterPosition(), composition),
                composition -> onCompositionClickListener.onItemClick(getBindingAdapterPosition(), composition)
        );
        binding.btnActionsMenu.setOnClickListener(v ->
                menuClickListener.onItemClick(getBindingAdapterPosition(), composition)
        );

        if (onLongClickListener != null) {
            binding.clickableItem.setOnLongClickListener(v -> {
                if (selected) {
                    return false;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getBindingAdapterPosition(), composition);
                return true;
            });
        }
    }

    public void bind(@Nonnull Composition composition, boolean isCoversEnabled) {
        this.composition = composition;
        compositionItemWrapper.bind(composition, isCoversEnabled);
    }

    public void update(Composition composition, List<Object> payloads) {
        this.composition = composition;
        compositionItemWrapper.update(composition, payloads);
    }

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
    public void onSwipeStateChanged(float swipeOffset) {
        compositionItemWrapper.showAsSwipingItem(swipeOffset);
    }

    public void showCurrentComposition(@Nullable CurrentComposition currentComposition,
                                       boolean animate) {
        boolean isCurrent = false;
        boolean isPlaying = false;
        if (currentComposition != null) {
            isCurrent = composition.equals(currentComposition.getComposition());
            isPlaying = isCurrent && currentComposition.isPlaying();
        }
        showAsCurrentComposition(isCurrent);
        compositionItemWrapper.showAsPlaying(isPlaying, animate);
    }

    public Composition getComposition() {
        return composition;
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
