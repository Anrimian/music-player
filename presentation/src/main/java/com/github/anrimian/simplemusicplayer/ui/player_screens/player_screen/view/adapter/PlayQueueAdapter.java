package com.github.anrimian.simplemusicplayer.ui.player_screens.player_screen.view.adapter;

import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import java.util.List;

import javax.annotation.Nullable;

/**
 * Created on 31.10.2017.
 */

public class PlayQueueAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private static final String CURRENT_COMPOSITION_CHANGED = "current_composition_changed";

    private final List<Composition> musicList;
    private OnItemClickListener<Composition> onCompositionClickListener;

    @Nullable
    private Composition currentComposition;

    public PlayQueueAdapter(List<Composition> musicList) {
        this.musicList = musicList;
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_playlist_music, parent, false);
        return new MusicViewHolder(view, onCompositionClickListener);
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position) {
        Composition composition = musicList.get(position);
        holder.bind(composition);
        holder.showAsPlayingComposition(composition.equals(currentComposition));
    }

    @Override
    public void onBindViewHolder(MusicViewHolder holder, int position, List<Object> payloads) {
        boolean updated = false;
        for (Object payload: payloads) {
            if (payload == CURRENT_COMPOSITION_CHANGED) {
                Composition composition = musicList.get(position);
                holder.showAsPlayingComposition(composition.equals(currentComposition));
                updated = true;
            }
        }
        if (!updated) {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    public void onCurrentCompositionChanged(Composition composition, int position) {
        int oldPosition = musicList.indexOf(currentComposition);
        currentComposition = composition;
        if (oldPosition != -1) {
            notifyItemChanged(oldPosition, CURRENT_COMPOSITION_CHANGED);
        }
        notifyItemChanged(position, CURRENT_COMPOSITION_CHANGED);

    }

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void updatePlayList(List<Composition> playList, List<Composition> newPlayList) {
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return playList.size();
            }

            @Override
            public int getNewListSize() {
                return newPlayList.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return playList.get(oldItemPosition).equals(newPlayList.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return playList.get(oldItemPosition).equals(newPlayList.get(newItemPosition));
            }
        });
        result.dispatchUpdatesTo(this);
    }
}
