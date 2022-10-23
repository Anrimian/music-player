package com.github.anrimian.musicplayer.ui.library.albums.list.adapter

import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ItemAlbumBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.BaseViewHolder

class AlbumViewHolder(
    parent: ViewGroup,
    itemClickListener: (Album) -> Unit,
    onItemMenuClickListener: (View, Album) -> Unit
) : BaseViewHolder(parent, R.layout.item_album) {

    private val viewBinding = ItemAlbumBinding.bind(itemView)

    private lateinit var album: Album

    init {
        viewBinding.clickableItem.setOnClickListener { itemClickListener(album) }
        viewBinding.btnActionsMenu.setOnClickListener { v -> onItemMenuClickListener(v, album) }
    }

    override fun release() {
        Components.getAppComponent().imageLoader().clearImage(viewBinding.ivMusicIcon)
    }

    fun bind(album: Album) {
        this.album = album
        showAlbumName()
        showAdditionalInfo()
        showCover()
    }

    fun update(album: Album, payloads: List<*>) {
        this.album = album
        for (payload in payloads) {
            if (payload is List<*>) {
                update(album, payload)
            }
            if (payload === Payloads.NAME) {
                showAlbumName()
                continue
            }
            if (payload === Payloads.ARTIST) {
                showAdditionalInfo()
                continue
            }
            if (payload === Payloads.COMPOSITIONS_COUNT) {
                showAdditionalInfo()
            }
        }
    }

    private fun showAlbumName() {
        val name = album.name
        viewBinding.tvAlbumName.text = name
        viewBinding.clickableItem.contentDescription = name
    }

    private fun showAdditionalInfo() {
        viewBinding.tvCompositionsCount.text = FormatUtils.formatAlbumAdditionalInfo(context, album)
    }

    private fun showCover() {
        Components.getAppComponent().imageLoader().displayImage(
            viewBinding.ivMusicIcon,
            album,
            R.drawable.ic_album_placeholder
        )
    }
}