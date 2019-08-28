package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;
import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;

/**
 * Created on 31.10.2017.
 */

public class MusicFileViewHolder extends FileViewHolder {

    @BindView(R.id.clickable_item)
    FrameLayout clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private MusicFileSource fileSource;

    private boolean selected = false;
    private boolean selectedToMove = false;
    private boolean playing = false;

    public MusicFileViewHolder(ViewGroup parent,
                               OnPositionItemClickListener<MusicFileSource> onCompositionClickListener,
                               OnViewItemClickListener<MusicFileSource> onMenuClickListener,
                               OnPositionItemClickListener<FileSource> onLongClickListener) {
        super(parent, R.layout.item_storage_music);
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition(), fileSource)
            );
        }
        if (onLongClickListener != null) {
            clickableItem.setOnLongClickListener(v -> {
                if (selected) {
                    return false;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getAdapterPosition(), fileSource);
                return true;
            });
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, fileSource));
    }

    public void bind(@Nonnull MusicFileSource fileSource, boolean isCoversEnabled) {
        this.fileSource = fileSource;
        compositionItemWrapper.bind(fileSource.getComposition(), isCoversEnabled);
    }

    public void update(MusicFileSource fileSource, List<Object> payloads) {
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

    public void setCoversVisible(boolean isCoversEnabled) {
        compositionItemWrapper.showCompositionImage(isCoversEnabled);
    }

    @Override
    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            if (!selected && playing) {
                showAsPlaying(true);
            } else {
                int unselectedColor = Color.TRANSPARENT;
                int selectedColor = getSelectionColor();
                int endColor = selected ? selectedColor : unselectedColor;
                animateBackgroundColor(clickableItem, endColor);
            }
        }
    }

    @Override
    public void setSelectedToMove(boolean selected) {
        if (this.selectedToMove != selected) {
            this.selectedToMove = selected;
            int unselectedColor = Color.TRANSPARENT;
            int selectedColor = getMoveSelectionColor();
            int endColor = selected ? selectedColor : unselectedColor;
            animateBackgroundColor(itemView, endColor);
        }
    }

    @Override
    public FileSource getFileSource() {
        return fileSource;
    }

    public void setPlaying(boolean playing) {
        if (this.playing != playing) {
            this.playing = playing;
            if (!selected) {
                showAsPlaying(playing);
            }
        }
    }

    public Composition getComposition() {
        return fileSource.getComposition();
    }

    private void showAsPlaying(boolean playing) {
        int unselectedColor = Color.TRANSPARENT;
        int selectedColor = getPlaySelectionColor();
        int endColor = playing ? selectedColor : unselectedColor;
        animateBackgroundColor(clickableItem, endColor);
    }

    private void selectImmediate() {
        clickableItem.setBackgroundColor(getSelectionColor());
        selected = true;
    }

    @ColorInt
    private int getPlaySelectionColor() {
        return getPlayingCompositionColor(getContext(), 20);
    }
}
