package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import javax.annotation.Nonnull;

import androidx.annotation.ColorInt;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

import static androidx.core.graphics.ColorUtils.setAlphaComponent;
import static com.github.anrimian.musicplayer.ui.utils.AndroidUtils.getColorFromAttr;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.animateColor;

/**
 * Created on 31.10.2017.
 */

public class MusicViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.clickable_item)
    FrameLayout clickableItem;

    @BindView(R.id.btn_actions_menu)
    View btnActionsMenu;

    private CompositionItemWrapper compositionItemWrapper;

    private Composition composition;
    private Drawable foregroundDrawable;
    private boolean selected = false;

    public MusicViewHolder(LayoutInflater inflater,
                           ViewGroup parent,
                           OnPositionItemClickListener<Composition> onCompositionClickListener,
                           OnViewItemClickListener<Composition> onMenuClickListener,
                           OnPositionItemClickListener<Composition> onLongClickListener) {
        super(inflater.inflate(R.layout.item_storage_music, parent, false));
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

    public void bind(@Nonnull Composition composition) {
        this.composition = composition;
        compositionItemWrapper.bind(composition);
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            int unselectedColor = Color.TRANSPARENT;
            int selectedColor = getSelectionColor();
            int startColor = selected ? unselectedColor : selectedColor;
            int endColor = selected ? selectedColor : unselectedColor;
            animateColor(startColor, endColor, color -> clickableItem.setBackgroundColor(color));

            setBackgroundClickEffectEnabled(!selected);
        }
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
    private int getSelectionColor() {
        return setAlphaComponent(getColorFromAttr(getContext(), R.attr.colorAccent), 25);
    }

    private Context getContext() {
        return itemView.getContext();
    }
}
