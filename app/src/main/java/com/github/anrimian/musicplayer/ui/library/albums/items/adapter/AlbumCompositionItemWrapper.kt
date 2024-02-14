package com.github.anrimian.musicplayer.ui.library.albums.items.adapter

import android.text.SpannableStringBuilder
import android.view.View
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.Payloads
import com.github.anrimian.musicplayer.domain.models.albums.AlbumComposition
import com.github.anrimian.musicplayer.ui.common.format.wrappers.CompositionItemWrapper

class AlbumCompositionItemWrapper(
    itemView: View,
    onIconClickListener: (AlbumComposition) -> Unit,
    onClickListener: (AlbumComposition) -> Unit
): CompositionItemWrapper<AlbumComposition>(
    itemView, onIconClickListener, onClickListener
) {
    override fun onUpdate(payload: Any?) {
        super.onUpdate(payload)
        if (payload == Payloads.TRACK_NUMBER || payload == Payloads.DISC_NUMBER) {
            showAdditionalInfo()
        }
    }

    override fun getAdditionalInfo(sb: SpannableStringBuilder) {
        val discNumber = composition.discNumber
        if (discNumber != null) {
            sb.append(getContext().getString(R.string.disc_short, discNumber))
        }
        val trackNumber = composition.trackNumber
        if (trackNumber != null) {
            sb.append(getContext().getString(R.string.track_short, trackNumber))
        }
        super.getAdditionalInfo(sb)
    }

}