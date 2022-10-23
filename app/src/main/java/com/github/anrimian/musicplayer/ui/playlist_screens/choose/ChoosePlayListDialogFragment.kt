package com.github.anrimian.musicplayer.ui.playlist_screens.choose

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.AttrRes
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSelectPlayListBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.utils.functions.BiCallback
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.playlist_screens.create.CreatePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.playlist_screens.playlists.adapter.PlayListsAdapter
import com.github.anrimian.musicplayer.ui.playlist_screens.rename.RenamePlayListDialogFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.OnCompleteListener
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback
import com.github.anrimian.musicplayer.ui.utils.views.delegate.*
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import moxy.MvpBottomSheetDialogFragment
import moxy.ktx.moxyPresenter

fun newChoosePlayListDialogFragment(extra: Bundle?): ChoosePlayListDialogFragment {
    return newChoosePlayListDialogFragment(0, extra)
}

@JvmOverloads
fun newChoosePlayListDialogFragment(
    @AttrRes statusBarColorAttr: Int,
    extra: Bundle? = null
) = ChoosePlayListDialogFragment().apply {
    val args = Bundle()
    args.putInt(Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG, statusBarColorAttr)
    args.putBundle(Constants.Arguments.EXTRA_DATA_ARG, extra)
    arguments = args
}


class ChoosePlayListDialogFragment : MvpBottomSheetDialogFragment(), ChoosePlayListView {

    private val presenter by moxyPresenter { Components.getAppComponent().choosePlayListPresenter() }

    private lateinit var viewBinding: DialogSelectPlayListBinding

    private lateinit var adapter: PlayListsAdapter
    private lateinit var slideDelegate: SlideDelegate

    private var onCompleteListener: OnCompleteListener<PlayList>? = null
    private var complexCompleteListener: BiCallback<PlayList, Bundle>? = null

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        viewBinding = DialogSelectPlayListBinding.inflate(LayoutInflater.from(context))
        val view = viewBinding.root
        dialog.setContentView(view)

        val displayMetrics = requireActivity().resources.displayMetrics
        val height = displayMetrics.heightPixels
        val heightPercent = AndroidUtils.getFloat(resources, R.dimen.choose_playlist_dialog_height)
        val minHeight = (height * heightPercent).toInt()
        view.minimumHeight = minHeight

        val layoutManager = LinearLayoutManager(context)
        viewBinding.recyclerView.layoutManager = layoutManager
        adapter = PlayListsAdapter(
            viewBinding.recyclerView,
            this::onPlayListSelected,
            this::onPlaylistMenuClicked
        )
        viewBinding.recyclerView.adapter = adapter

        RecyclerViewUtils.attachDynamicShadow(viewBinding.recyclerView, viewBinding.titleShadow)

        val bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog)
        bottomSheetBehavior.peekHeight = minHeight
        bottomSheetBehavior.addBottomSheetCallback(SimpleBottomSheetCallback({ newState: Int ->
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss()
            }
        }, presenter::onBottomSheetSlided))
        slideDelegate = buildSlideDelegate()

        viewBinding.ivClose.setOnClickListener { dismiss() }
        viewBinding.ivClose.visibility = View.INVISIBLE //start state
        viewBinding.ivCreatePlaylist.setOnClickListener { onCreatePlayListButtonClicked() }

        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground)
    }

    override fun onResume() {
        super.onResume()
        DialogUtils.setupBottomSheetDialogMaxWidth(this)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        slideDelegate.onSlide(0f)
    }

    override fun showBottomSheetSlided(slideOffset: Float) {
        viewBinding.recyclerView.post {
            val contentView = AndroidUtils.getContentView(activity) ?: return@post

            var usableSlideOffset = slideOffset
            val activityHeight =
                contentView.height - AndroidUtils.getStatusBarHeight(requireContext())
            val viewHeight = viewBinding.listContainer.height
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0f
            }
            slideDelegate.onSlide(usableSlideOffset)
        }
    }

    override fun showEmptyList() {
        viewBinding.progressStateView.showMessage(R.string.play_lists_on_device_not_found, false)
    }

    override fun showList() {
        viewBinding.progressStateView.hideAll()
    }

    override fun showLoading() {
        viewBinding.progressStateView.showProgress()
    }

    override fun updateList(list: List<PlayList>) {
        adapter.submitList(list)
    }

    override fun showConfirmDeletePlayListDialog(playList: PlayList) {
        showConfirmDeleteDialog(requireContext(), playList) {
            presenter.onDeletePlayListDialogConfirmed(playList)
        }
    }

    override fun showEditPlayListNameDialog(playList: PlayList) {
        val fragment = RenamePlayListDialogFragment.newInstance(playList.id)
        fragment.safeShow(childFragmentManager)
    }

    override fun showPlayListDeleteSuccess(playList: PlayList) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.play_list_deleted, playList.name),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun showDeletePlayListError(errorCommand: ErrorCommand) {
        MessagesUtils.makeSnackbar(
            viewBinding.listContainer,
            getString(R.string.play_list_delete_error, errorCommand.message),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun onPlaylistMenuClicked(playList: PlayList, view: View) {
        PopupMenuWindow.showPopup(view, R.menu.play_list_menu) { menuItem ->
            when (menuItem.itemId) {
                R.id.menu_change_play_list_name -> {
                    presenter.onChangePlayListNameButtonClicked(playList)
                }
                R.id.menu_delete_play_list -> {
                    presenter.onDeletePlayListButtonClicked(playList)
                }
            }
        }
    }

    fun setOnCompleteListener(onCompleteListener: OnCompleteListener<PlayList>) {
        this.onCompleteListener = onCompleteListener
    }

    fun setComplexCompleteListener(complexCompleteListener: BiCallback<PlayList, Bundle>) {
        this.complexCompleteListener = complexCompleteListener
    }

    private fun onPlayListSelected(playList: PlayList) {
        onCompleteListener?.onComplete(playList)

        complexCompleteListener?.call(
            playList,
            requireArguments().getBundle(Constants.Arguments.EXTRA_DATA_ARG)
        )

        dismissAllowingStateLoss()
    }

    private fun onCreatePlayListButtonClicked() {
        val fragment = CreatePlayListDialogFragment()
        fragment.safeShow(childFragmentManager)
    }

    private fun buildSlideDelegate(): SlideDelegate {
        val boundDelegate: SlideDelegate = DelegateManager()
            .addDelegate(
                BoundValuesDelegate(0.85f, 1f, VisibilityDelegate(viewBinding.ivClose))
            )
            .addDelegate(
                BoundValuesDelegate(
                    0.7f,
                    1f,
                    DelegateManager()
                        .addDelegate(MotionLayoutDelegate(viewBinding.motionLayout))
                        .addDelegate(
                            TextSizeDelegate(
                                viewBinding.tvTitle,
                                R.dimen.sheet_dialog_title_collapsed_size,
                                R.dimen.sheet_dialog_title_expanded_size
                            )
                        )
                        .addDelegate(
                            TextColorDelegate(
                                viewBinding.tvTitle,
                                android.R.attr.textColorSecondary,
                                android.R.attr.textColorPrimary
                            )
                        )
                )
            )
        return DelegateManager()
            .addDelegate(BoundValuesDelegate(0.008f, 0.95f, boundDelegate))
            .addDelegate(
                BoundValuesDelegate(
                    0.85f,
                    1f,
                    StatusBarColorDelegate(
                        requireActivity().window,
                        AndroidUtils.getColorFromAttr(context, getStatusBarColorAttr()),
                        AndroidUtils.getColorFromAttr(
                            requireContext(),
                            R.attr.colorPrimaryDarkSecondary
                        )
                    )
                )
            )
    }

    @AttrRes
    private fun getStatusBarColorAttr(): Int {
        val args = arguments
        if (args != null) {
            val colorAttr = args.getInt(Constants.Arguments.STATUS_BAR_COLOR_ATTR_ARG)
            if (colorAttr != 0) {
                return colorAttr
            }
        }
        return android.R.attr.statusBarColor
    }

}