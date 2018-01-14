package com.github.anrimian.simplemusicplayer.ui.player.main.view.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.Composition;
import com.github.anrimian.simplemusicplayer.utils.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 31.10.2017.
 */

public class PlayListAdapter extends RecyclerView.Adapter<MusicViewHolder> {

    private List<Composition> musicList = new ArrayList<>();
    private OnItemClickListener<Composition> onCompositionClickListener;

    public PlayListAdapter(List<Composition> musicList) {
        this.musicList = musicList;
    }

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
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
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }
}
