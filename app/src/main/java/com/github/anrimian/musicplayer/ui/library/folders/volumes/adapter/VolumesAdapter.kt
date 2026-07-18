package com.github.anrimian.musicplayer.ui.library.folders.volumes.adapter

import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.folders.Volume
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.diff_utils.SimpleDiffItemCallback
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.mvp.MvpDiffAdapter

class VolumesAdapter(
    lifecycleOwner: LifecycleOwner,
    recyclerView: RecyclerView,
    private val selectedItems: HashSet<Volume>,
    private val onItemClickListener: (Int, Volume) -> Unit,
    private val onLongClickListener: (Int, Volume) -> Unit,
    private val onMenuClickListener: (View, Volume) -> Unit
) : MvpDiffAdapter<Volume, VolumeViewHolder>(
    lifecycleOwner,
    recyclerView,
    SimpleDiffItemCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VolumeViewHolder {
        return VolumeViewHolder(
            parent,
            onItemClickListener,
            onMenuClickListener,
            onLongClickListener
        )
    }

    override fun onBindViewHolder(holder: VolumeViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)
        val volume = getItem(position)
        holder.bind(volume)
        holder.setSelected(selectedItems.contains(volume))
    }

    override fun onBindViewHolder(
        holder: VolumeViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
            return
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
        forEachHolder { holder ->
            holder.setSelected(selected)
        }
    }

}