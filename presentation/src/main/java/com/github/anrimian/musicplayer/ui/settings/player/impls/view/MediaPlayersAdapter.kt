package com.github.anrimian.musicplayer.ui.settings.player.impls.view

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class MediaPlayersAdapter(
    private val mediaPlayers: IntArray
): RecyclerView.Adapter<MediaPlayerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = MediaPlayerViewHolder(parent)

    override fun onBindViewHolder(holder: MediaPlayerViewHolder, position: Int) {
        holder.bind(mediaPlayers[position])
    }

    override fun getItemCount() = mediaPlayers.size
}