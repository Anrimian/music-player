package com.github.anrimian.musicplayer.ui.playlist_screens.playlist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.playlist.PlayListItem
import com.github.anrimian.musicplayer.domain.models.utils.PlayListItemHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
class PlayListItemAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val coversEnabled: Boolean,
    private val menuClickListener: (View, Int, PlayListItem) -> Unit,
    private val onItemClickListener: (PlayListItem, Int) -> Unit
) : MvpDiffAdapter<PlayListItem, PlayListItemViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(PlayListItemHelper::areSourcesTheSame, PlayListItemHelper::getChangePayload)
) {

    private var currentComposition: CurrentComposition? = null
    private var syncStates = emptyMap<Long, FileSyncState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListItemViewHolder {
        return PlayListItemViewHolder(
            LayoutInflater.from(parent.context),
            parent,
            menuClickListener,
            onItemClickListener
        )
    }

    override fun onBindViewHolder(holder: PlayListItemViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val composition = getItem(position)
        holder.bind(composition, coversEnabled)
        holder.showCurrentComposition(currentComposition, false)
        holder.setFileSyncStates(syncStates)
    }

    fun showCurrentComposition(currentComposition: CurrentComposition?) {
        this.currentComposition = currentComposition
        forEachHolder { holder ->
            holder.showCurrentComposition(currentComposition, true)
        }
    }

    fun showFileSyncStates(states: Map<Long, FileSyncState>) {
        this.syncStates = states
        forEachHolder { holder ->
            holder.setFileSyncStates(syncStates)
        }
    }
}