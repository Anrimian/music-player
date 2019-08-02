package com.github.anrimian.musicplayer.ui.library.folders.adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.FolderFileSource;
import com.github.anrimian.musicplayer.domain.models.composition.folders.MusicFileSource;
import com.github.anrimian.musicplayer.domain.models.utils.FolderHelper;
import com.github.anrimian.musicplayer.ui.utils.OnItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnPositionItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.OnViewItemClickListener;
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created on 31.10.2017.
 */

public class MusicFileSourceAdapter extends ListAdapter<FileSource, RecyclerView.ViewHolder> {

    private static final int TYPE_MUSIC = 1;
    private static final int TYPE_FILE = 2;

    private final Set<RecyclerView.ViewHolder> viewHolders = new HashSet<>();

    private OnPositionItemClickListener<Composition> onCompositionClickListener;
    private OnItemClickListener<String> onFolderClickListener;
    private OnViewItemClickListener<FolderFileSource> onFolderMenuClickListener;
    private OnViewItemClickListener<Composition> onCompositionMenuItemClicked;

    @Nullable
    private Composition currentComposition;
    private boolean isCoversEnabled;

    public MusicFileSourceAdapter() {
        super(new SimpleDiffItemCallback<>(FolderHelper::areSourcesTheSame));
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        switch (type) {
            case TYPE_MUSIC: {
                return new MusicViewHolder(parent,
                        onCompositionClickListener,
                        onCompositionMenuItemClicked,
                        null);
            }
            case TYPE_FILE: {
                return new FolderViewHolder(parent,
                        onFolderClickListener,
                        onFolderMenuClickListener);
            }
            default: throw new IllegalStateException("unexpected item type: " + type);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        viewHolders.add(holder);

        FileSource fileSource = getItem(position);
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder,
                                 int position,
                                 @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position);
            return;
        }
        FileSource fileSource = getItem(position);
        switch (holder.getItemViewType()) {
            case TYPE_MUSIC: {
                MusicViewHolder musicViewHolder = (MusicViewHolder) holder;
                MusicFileSource musicFileSource = (MusicFileSource) fileSource;
                Composition composition = musicFileSource.getComposition();
                musicViewHolder.update(composition, payloads);
                break;
            }
            case TYPE_FILE: {
                FolderViewHolder folderViewHolder = (FolderViewHolder) holder;
                FolderFileSource folderFileSource = (FolderFileSource) fileSource;
                folderViewHolder.update(folderFileSource, payloads);
                break;
            }
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        viewHolders.remove(holder);
    }

    @Override
    public int getItemViewType(int position) {
        FileSource source = getItem(position);
        if (source instanceof FolderFileSource) {
            return TYPE_FILE;
        } else {
            return TYPE_MUSIC;
        }
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
