package com.github.anrimian.musicplayer.ui.library.compositions.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_SELECTED;
import static com.github.anrimian.musicplayer.domain.Payloads.ITEM_UNSELECTED;

/**
 * Created on 31.10.2017.
 */

public class CompositionsAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private final Set<MusicViewHolder> viewHolders = new HashSet<>();

    private List<Composition> musicList;
    private final HashSet<Composition> selectedCompositions;
    private OnPositionItemClickListener<Composition> onCompositionClickListener;
    private OnViewItemClickListener<Composition> onMenuItemClickListener;
    private OnPositionItemClickListener<Composition> onLongClickListener;

    @Nullable
    private Composition currentComposition;
    private boolean play;
    private boolean isCoversEnabled;

    public CompositionsAdapter(List<Composition> musicList,
                               HashSet<Composition> selectedCompositions) {
        this.musicList = musicList;
        this.selectedCompositions = selectedCompositions;
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MusicViewHolder(parent,
                onCompositionClickListener,
                onMenuItemClickListener,
                onLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder, int position) {
        viewHolders.add(holder);

        Composition composition = musicList.get(position);
        holder.bind(composition, isCoversEnabled);
        boolean selected = selectedCompositions.contains(composition);
        holder.setSelected(selected);

        boolean isCurrentComposition = composition.equals(currentComposition);
        holder.showAsCurrentComposition(isCurrentComposition);
        holder.showAsPlaying(isCurrentComposition && play);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        for (Object payload: payloads) {
            if (payload == ITEM_SELECTED) {
                holder.setSelected(true);
                return;
            }
            if (payload == ITEM_UNSELECTED) {
                holder.setSelected(false);
                return;
            }
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @Override
    public void onViewRecycled(@NonNull MusicViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    public void setItems(List<Composition> list) {
        musicList = list;
    }

    public void setItemSelected(int position) {
        notifyItemChanged(position, ITEM_SELECTED);
    }

    public void setItemUnselected(int position) {
        notifyItemChanged(position, ITEM_UNSELECTED);
    }

    public void setItemsSelected(boolean selected) {
        for (MusicViewHolder holder: viewHolders) {
            holder.setSelected(selected);
        }
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnMenuItemClickListener(OnViewItemClickListener<Composition> onMenuItemClickListener) {
        this.onMenuItemClickListener = onMenuItemClickListener;
    }

    public void setOnLongClickListener(OnPositionItemClickListener<Composition> onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }

    public void showCurrentComposition(Composition currentComposition) {
        this.currentComposition = currentComposition;
        for (MusicViewHolder holder: viewHolders) {
            Composition composition = holder.getComposition();
            boolean isCurrentComposition = composition.equals(currentComposition);
            holder.showAsCurrentComposition(isCurrentComposition);
            holder.showAsPlaying(isCurrentComposition && play);
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (MusicViewHolder holder: viewHolders) {
            holder.setCoversVisible(isCoversEnabled);
        }
    }

    public void showPlaying(boolean play) {
        this.play = play;
        for (MusicViewHolder holder: viewHolders) {
            Composition composition = holder.getComposition();
            boolean isCurrentComposition = composition.equals(currentComposition);
            holder.showAsCurrentComposition(isCurrentComposition);
            holder.showAsPlaying(isCurrentComposition && play);
        }
    }
}
