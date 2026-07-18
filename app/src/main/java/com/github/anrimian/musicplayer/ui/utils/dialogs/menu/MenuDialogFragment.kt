package com.github.anrimian.musicplayer.ui.utils.dialogs.menu

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.MenuRes
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogMenuBinding
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.args

class MenuDialogFragment : DialogFragment() {

    private var onCompleteListener: ((MenuItem) -> Unit)? = null

    private var complexCompleteListener: ((MenuItem, Bundle) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogMenuBinding.inflate(LayoutInflater.from(requireContext()))
        val view: View = binding.root

        binding.rvMenuItems.layoutManager = LinearLayoutManager(requireContext())

        val menu = AndroidUtils.createMenu(requireContext(), args.getInt(MENU_ARG))
        val menuAdapter = MenuAdapter(menu, R.layout.item_dialog_menu, this::onMenuItemClicked)
        binding.rvMenuItems.adapter = menuAdapter

        return AlertDialog.Builder(activity)
            .setTitle(args.getString(TITLE_ARG))
            .setView(view)
            .create()
    }

    fun setOnCompleteListener(onCompleteListener: (MenuItem) -> Unit) {
        this.onCompleteListener = onCompleteListener
    }

    fun setComplexCompleteListener(complexCompleteListener: (MenuItem, Bundle) -> Unit) {
        this.complexCompleteListener = complexCompleteListener
    }

    private fun onMenuItemClicked(menuItem: MenuItem) {
        onCompleteListener?.invoke(menuItem)
        complexCompleteListener?.invoke(
            menuItem,
            args.getBundle(AppConstants.Arguments.EXTRA_DATA_ARG)!!
        )
        dismissAllowingStateLoss()
    }

    companion object {

        private const val MENU_ARG = "menu_arg"
        private const val TITLE_ARG = "title_arg"

        fun newInstance(
            @MenuRes menuRes: Int,
            title: String?,
            extra: Bundle? = null
        ) = MenuDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(MENU_ARG, menuRes)
                putString(TITLE_ARG, title)
                putBundle(AppConstants.Arguments.EXTRA_DATA_ARG, extra)
            }
        }

    }

}
