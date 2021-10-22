package com.github.anrimian.musicplayer.ui.settings.player.impls

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogEnabledMediaPlayersBinding
import com.github.anrimian.musicplayer.di.Components
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class EnabledMediaPlayersDialogFragment: MvpAppCompatDialogFragment(), EnabledMediaPlayersView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().enabledMediaPlayersPresenter() }

    private lateinit var viewBinding: DialogEnabledMediaPlayersBinding

    var onCompleteListener: ((IntArray) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogEnabledMediaPlayersBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.enabled_media_players)
            .setView(viewBinding.root)
            .create()
        dialog.show()

        return dialog
    }

    override fun showMediaPlayers(mediaPlayers: IntArray) {

    }

    override fun showEnabledMediaPlayers(mediaPlayers: IntArray) {

    }

    override fun close(result: IntArray) {
        onCompleteListener?.invoke(result)
        dismissAllowingStateLoss()
    }
}