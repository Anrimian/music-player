package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MediaPlayersAdapter(
    private val mediaPlayers: List<Int>,
    private val onPlayerEnabled: (Int, Boolean) -> Unit,
    private val onDragButtonClick: (RecyclerView.ViewHolder) -> Unit
): RecyclerView.Adapter<MediaPlayerViewHolder>() {

    private val viewHolders = HashSet<MediaPlayerViewHolder>()

    private var enabledItems: Set<Int> = HashSet()
    private var isDisableAllowed: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MediaPlayerViewHolder(parent, onPlayerEnabled, onDragButtonClick)

    override fun onBindViewHolder(holder: MediaPlayerViewHolder, position: Int) {
        viewHolders.add(holder)

        val item = mediaPlayers[position]
        holder.bind(item)
        holder.setEnabled(enabledItems)
        holder.setDisableAllowed(isDisableAllowed)
    }

    override fun getItemCount() = mediaPlayers.size

    override fun onViewRecycled(holder: MediaPlayerViewHolder) {
        super.onViewRecycled(holder)
        viewHolders.remove(holder)
    }

    fun setEnabledItems(enabledItems: Set<Int>) {
        this.enabledItems = enabledItems
        viewHolders.forEach { holder -> holder.setEnabled(enabledItems) }
    }

    fun setDisableAllowed(allowed: Boolean) {
        isDisableAllowed = allowed
        viewHolders.forEach { holder -> holder.setDisableAllowed(allowed) }
    }
}