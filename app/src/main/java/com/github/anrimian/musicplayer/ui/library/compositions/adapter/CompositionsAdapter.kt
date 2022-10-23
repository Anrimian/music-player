package com.github.anrimian.musicplayer.ui.library.compositions.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CurrentComposition
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

/**
 * Created on 31.10.2017.
 */
class CompositionsAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedCompositions: Set<Composition>,
    private val onCompositionClickListener: (Int, Composition) -> Unit,
    private val onLongClickListener: (Int, Composition) -> Unit,
    private val iconClickListener: (Int, Composition) -> Unit,
    private val menuClickListener: (View, Int, Composition) -> Unit
) : MvpDiffAdapter<Composition, MusicViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback(CompositionHelper::areSourcesTheSame, CompositionHelper::getChangePayload)
) {

    private var currentComposition: CurrentComposition? = null
    private var isCoversEnabled = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        return MusicViewHolder(
            parent,
            onCompositionClickListener,
            onLongClickListener,
            iconClickListener,
            menuClickListener
        )
    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val composition = getItem(position)
        holder.bind(composition, isCoversEnabled)

        val selected = selectedCompositions.contains(composition)
        holder.setSelected(selected)
        holder.showCurrentComposition(currentComposition, false)
    }

    override fun onBindViewHolder(
        holder: MusicViewHolder,
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
}