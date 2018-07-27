package com.github.anrimian.simplemusicplayer.ui.library.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.domain.models.composition.Composition;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.simplemusicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffCallback;
import com.github.anrimian.simplemusicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.OnTransitionItemClickListener;
import com.github.anrimian.simplemusicplayer.ui.utils.views.recycler_view.endless_scrolling.HeaderFooterRecyclerViewAdapter;

import java.util.List;

import static android.support.v7.util.DiffUtil.calculateDiff;
import static com.github.anrimian.simplemusicplayer.domain.models.utils.CompositionHelper.hasChanges;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends HeaderFooterRecyclerViewAdapter {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final List<FileSource> musicList;
    private OnItemClickListener<Composition> onCompositionClickListener;
    private OnItemClickListener<Composition> onDeleteCompositionClickListener;
    private OnTransitionItemClickListener<String> onFolderClickListener;

    public MusicFileSourceAdapter(List<FileSource> musicList) {
        this.musicList = musicList;
    }

    public void setOnCompositionClickListener(OnItemClickListener<Composition> onCompositionClickListener) {
        this.onCompositionClickListener = onCompositionClickListener;
    }

    public void setOnFolderClickListener(OnTransitionItemClickListener<String> onFolderClickListener) {
        this.onFolderClickListener = onFolderClickListener;
    }

    public void setOnDeleteCompositionClickListener(OnItemClickListener<Composition> onDeleteCompositionClickListener) {
        this.onDeleteCompositionClickListener = onDeleteCompositionClickListener;
    }

    @Override
    public RecyclerView.ViewHolder createVH(ViewGroup parent, int type) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (type) {
            case TYPE_MUSIC: {
                View view = inflater.inflate(R.layout.item_storage_music, parent, false);
                return new MusicViewHolder(view,
                        onCompositionClickListener,
                        onDeleteCompositionClickListener);
            }
            case TYPE_FILE: {
                View view = inflater.inflate(R.layout.item_storage_folder, parent, false);
                return new FolderViewHolder(view, onFolderClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void bindVH(RecyclerView.ViewHolder holder, int position) {
        FileSource fileSource = musicList.get(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                MusicFileSource musicFileSource = (MusicFileSource) fileSource;
                musicViewHolder.bind(musicFileSource.getComposition());
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
    public int getCount() {
        return musicList.size();
    }

    @Override
    protected int getItemType(int position) {
        FileSource source = musicList.get(position);
        if (source instanceof FolderFileSource) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
    }

    public void updateList(List<FileSource> oldList, List<FileSource> sourceList) {
        calculateDiff(new SimpleDiffCallback<>(oldList, sourceList, this::areSourcedTheSame))
                .dispatchUpdatesTo(this);
    }

    private boolean areSourcedTheSame(FileSource oldSource, FileSource newSource) {
        if (oldSource.getClass().equals(newSource.getClass())) {
            if (oldSource instanceof FolderFileSource) {
                return !FolderHelper.hasChanges(((FolderFileSource) oldSource),
                        ((FolderFileSource) newSource));
            }
            if (oldSource instanceof MusicFileSource) {
                return !hasChanges(((MusicFileSource) oldSource).getComposition(),
                        ((MusicFileSource) newSource).getComposition());
            }
        }
        return false;
    }
}
