package com.github.anrimian.musicplayer.ui.library.compositions.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
open class CompositionsAdapter<T : Composition>(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedCompositions: Set<T>,
    private val onCompositionClickListener: (Int, T) -> Unit,
    private val onLongClickListener: (Int, T) -> Unit,
    private val iconClickListener: (Int, T) -> Unit,
    private val menuClickListener: (View, Int, T) -> Unit,
    diffCallback: DiffUtil.ItemCallback<T> = SimpleDiffItemCallback(
        CompositionHelper::areSourcesTheSame,
        CompositionHelper::getChangePayload
    )
) : MvpDiffAdapter<T, CompositionViewHolder<T>>(
    lifecycleOwner,
    recyclerView,
    diffCallback
) {

    private var currentComposition: CurrentComposition? = null
    private var isCoversEnabled = false
    private var syncStates = emptyMap<Long, FileSyncState>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompositionViewHolder<T> {
        return CompositionViewHolder(
            parent,
            onCompositionClickListener,
            onLongClickListener,
            iconClickListener,
            menuClickListener
        )
    }

    override fun onBindViewHolder(holder: CompositionViewHolder<T>, position: Int) {
        super.onBindViewHolder(holder, position)

        val composition = getItem(position)
        holder.bind(composition, isCoversEnabled)

        val selected = selectedCompositions.contains(composition)
        holder.setSelected(selected)
        holder.showCurrentComposition(currentComposition, false)
        holder.setFileSyncStates(syncStates)
    }

    override fun onBindViewHolder(
        holder: CompositionViewHolder<T>,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
            return
        }
        for (payload in payloads) {
            if (payload === Payloads.ITEM_SELECTED) {
                holder.setSelected(true)
                return
            }
            if (payload === Payloads.ITEM_UNSELECTED) {
                holder.setSelected(false)
                return
            }
        }
        holder.update(getItem(position), payloads)
    }

    fun setItemSelected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_SELECTED)
    }

    fun setItemUnselected(position: Int) {
        notifyItemChanged(position, Payloads.ITEM_UNSELECTED)
    }

    fun setItemsSelected(selected: Boolean) {
        forEachHolder { holder -> holder.setSelected(selected) }
    }

    fun showCurrentComposition(currentComposition: CurrentComposition?) {
        this.currentComposition = currentComposition
        forEachHolder { holder ->
            holder.showCurrentComposition(currentComposition, true)
        }
    }

    fun setCoversEnabled(isCoversEnabled: Boolean) {
        this.isCoversEnabled = isCoversEnabled
        forEachHolder { holder ->
            holder.setCoversVisible(isCoversEnabled)
        }
    }

    fun showFileSyncStates(states: Map<Long, FileSyncState>) {
        this.syncStates = states
        forEachHolder { holder ->
            holder.setFileSyncStates(syncStates)
        }
    }
}