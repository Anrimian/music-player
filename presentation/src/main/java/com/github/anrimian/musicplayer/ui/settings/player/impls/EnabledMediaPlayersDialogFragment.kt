package com.github.anrimian.musicplayer.ui.settings.player.impls

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogEnabledMediaPlayersBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.settings.player.impls.view.MediaPlayersAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.SimpleItemTouchHelperCallback
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

//add reset button?
//drag logic
//do not drag out of recycler view borders
//check files with unsupported seeking
class EnabledMediaPlayersDialogFragment: MvpAppCompatDialogFragment(), EnabledMediaPlayersView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().enabledMediaPlayersPresenter() }

    private lateinit var viewBinding: DialogEnabledMediaPlayersBinding

    private lateinit var adapter: MediaPlayersAdapter
    private lateinit var itemTouchHelper: ItemTouchHelper

    var onCompleteListener: ((IntArray) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogEnabledMediaPlayersBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.enabled_media_players)
            .setView(viewBinding.root)
            .setPositiveButton(android.R.string.ok) { _, _ -> presenter.onCompleteButtonClicked() }
            .setNegativeButton(R.string.close) { _, _ -> }
            .create()
        dialog.show()

        viewBinding.rvMediaPlayers.layoutManager = LinearLayoutManager(requireContext())

        val callback = SimpleItemTouchHelperCallback(false)
        callback.setOnMovedListener(presenter::onItemMoved)
        itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(viewBinding.rvMediaPlayers)

        return dialog
    }

    override fun showMediaPlayers(mediaPlayers: IntArray) {
        adapter = MediaPlayersAdapter(
            mediaPlayers,
            presenter::onItemEnableStatusChanged,
            itemTouchHelper::startDrag
        )
        viewBinding.rvMediaPlayers.adapter = adapter
    }

    override fun showEnabledMediaPlayers(mediaPlayers: Set<Int>) {
        adapter.setEnabledItems(mediaPlayers)
    }

    override fun setDisableAllowed(allowed: Boolean) {
        adapter.setDisableAllowed(allowed)
    }

    override fun close(result: IntArray) {
        onCompleteListener?.invoke(result)
        dismissAllowingStateLoss()
    }
}