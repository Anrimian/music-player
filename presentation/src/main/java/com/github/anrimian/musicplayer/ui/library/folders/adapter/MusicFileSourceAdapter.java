package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends RecyclerView.Adapter {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final Set<RecyclerView.ViewHolder> viewHolders = new HashSet<>();

    private List<FileSource> musicList;
    private OnPositionItemClickListener<Composition> onCompositionClickListener;
    private OnItemClickListener<String> onFolderClickListener;
    private OnViewItemClickListener<FolderFileSource> onFolderMenuClickListener;
    private OnViewItemClickListener<Composition> onCompositionMenuItemClicked;

    @Nullable
    private Composition currentComposition;
    private boolean isCoversEnabled;

    public MusicFileSourceAdapter(List<FileSource> musicList) {
        this.musicList = musicList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case TYPE_MUSIC: {
                return new MusicViewHolder(inflater,
                        parent,
                        onCompositionClickListener,
                        onCompositionMenuItemClicked,
                        null);
            }
            case TYPE_FILE: {
                return new FolderViewHolder(inflater,
                        parent,
                        onFolderClickListener,
                        onFolderMenuClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolders.add(holder);

        FileSource fileSource = musicList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                MusicFileSource musicFileSource = (MusicFileSource) fileSource;
                Composition composition = musicFileSource.getComposition();
                musicViewHolder.bind(composition, isCoversEnabled);
                musicViewHolder.setPlaying(composition.equals(currentComposition));
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource folderFileSource = (FolderFileSource) fileSource;
                folderViewHolder.bind(folderFileSource);
                break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        //noinspection unchecked
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    @Override
    public int getItemViewType(int position) {
        FileSource source = musicList.get(position);
        if (source instanceof FolderFileSource) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }

    public void setItems(List<FileSource> musicList) {
        this.musicList = musicList;
    }

    public void setOnCompositionClickListener(OnPositionItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnFolderClickListener(OnItemClickListener<String> onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    public void setOnCompositionMenuItemClicked(OnViewItemClickListener<Composition> onCompositionMenuItemClicked) {
        this.onCompositionMenuItemClicked = onCompositionMenuItemClicked;
    }

    public void setOnFolderMenuClickListener(OnViewItemClickListener<FolderFileSource> onFolderMenuClickListener) {
        this.onFolderMenuClickListener = onFolderMenuClickListener;
    }

    public void showPlayingComposition(Composition composition) {
        currentComposition = composition;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicViewHolder) {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                musicViewHolder.setPlaying(musicViewHolder.getComposition().equals(composition));
            }
        }
    }

    public void setCoversEnabled(boolean isCoversEnabled) {
        this.isCoversEnabled = isCoversEnabled;
        for (RecyclerView.ViewHolder holder: viewHolders) {
            if (holder instanceof MusicViewHolder) {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                musicViewHolder.setCoversVisible(isCoversEnabled);
            }
        }
    }
}
