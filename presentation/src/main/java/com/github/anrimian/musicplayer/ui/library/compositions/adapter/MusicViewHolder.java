package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SelectableViewHolder;

import java.util.List;

import javax.annotation.Nonnull;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.ui.common.format.ColorFormatUtils.getPlayingCompositionColor;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateBackgroundColor;

/**
 * Created on 31.10.2017.
 */

public class MusicViewHolder extends SelectableViewHolder {

    @BindView(R.id.clickable_item)
    FrameLayout clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private Composition composition;
    private Drawable foregroundDrawable;

    private boolean selected = false;
    private boolean playing = false;

    public MusicViewHolder(ViewGroup parent,
                           OnPositionItemClickListener<Composition> onCompositionClickListener,
                           OnViewItemClickListener<Composition> onMenuClickListener,
                           OnPositionItemClickListener<Composition> onLongClickListener) {
        super(parent, R.layout.item_storage_music);
        ButterKnife.bind(this, itemView);
        compositionItemWrapper = new CompositionItemWrapper(itemView);

        if (onCompositionClickListener != null) {
            clickableItem.setOnClickListener(v ->
                    onCompositionClickListener.onItemClick(getAdapterPosition(), composition)
            );
        }
        if (onLongClickListener != null) {
            clickableItem.setOnLongClickListener(v -> {
                if (selected) {
                    return false;
                }
                selectImmediate();
                onLongClickListener.onItemClick(getAdapterPosition(), composition);
                return true;
            });
        }
        btnActionsMenu.setOnClickListener(v -> onMenuClickListener.onItemClick(v, composition));
    }

    public void bind(@Nonnull Composition composition, boolean isCoversEnabled) {
        this.composition = composition;
        compositionItemWrapper.bind(composition, isCoversEnabled);
    }

    public void update(Composition composition, List<Object> payloads) {
        this.composition = composition;
        compositionItemWrapper.update(composition, payloads);
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
            setBackgroundClickEffectEnabled(!selected);
        }
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
        return composition;
    }

    private void showAsPlaying(boolean playing) {
        int unselectedColor = Color.TRANSPARENT;
        int selectedColor = getPlaySelectionColor();
        int endColor = playing ? selectedColor : unselectedColor;
        animateBackgroundColor(clickableItem, endColor);
    }

    private void selectImmediate() {
        setBackgroundClickEffectEnabled(false);
        clickableItem.setBackgroundColor(getSelectionColor());
        selected = true;
    }

    private void setBackgroundClickEffectEnabled(boolean enabled) {
        Drawable drawable = clickableItem.getForeground();
        if (drawable != null) {
            if (!enabled) {
                foregroundDrawable = drawable;
                clickableItem.setForeground(null);
            }
        } else if (enabled) {
            clickableItem.setForeground(foregroundDrawable);
        }
    }

    @ColorInt
    private int getPlaySelectionColor() {
        return getPlayingCompositionColor(getContext(), 20);
    }
}
