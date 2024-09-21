package com.github.anrimian.musicplayer.ui.widgets.menu

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.Constants.Arguments
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ActivityWidgetMenuBinding
import com.github.anrimian.musicplayer.databinding.PartialWidgetMenuHeaderBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.ui.common.activity.BaseMvpAppCompatActivity
import com.github.anrimian.musicplayer.ui.common.dialogs.shareComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.showConfirmDeleteDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.description.DescriptionSpannableStringBuilder
import com.github.anrimian.musicplayer.ui.editor.common.DeleteErrorHandler
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import moxy.ktx.moxyPresenter

class WidgetMenuActivity: BaseMvpAppCompatActivity(), WidgetMenuView {

    private val presenter by moxyPresenter { Components.getAppComponent().widgetMenuPresenter() }

    private lateinit var viewBinding: ActivityWidgetMenuBinding

    private lateinit var headerItem: SingleItemAdapter<PartialWidgetMenuHeaderBinding>

    private lateinit var deletingErrorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentTheme(this)
        theme.applyStyle(R.style.PopupActivityTheme, true)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityWidgetMenuBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.recyclerView.layoutManager = LinearLayoutManager(this)

        headerItem = SingleItemAdapter { inflater, root ->
            PartialWidgetMenuHeaderBinding.inflate(inflater, root, false)
        }
        val menu = AndroidUtils.createMenu(this, R.menu.widget_menu)
        val menuAdapter = MenuAdapter(menu, R.layout.item_popup_menu)
        menuAdapter.setOnItemClickListener(this::onMenuItemClicked)
        viewBinding.recyclerView.adapter = ConcatAdapter(headerItem, menuAdapter)

        deletingErrorHandler = DeleteErrorHandler(
            this,
            presenter::onRetryFailedDeleteActionClicked,
            this::showEditorRequestDeniedMessage
        )

        if (savedInstanceState == null) {
            applyCompositionId(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        applyCompositionId(intent)
    }

    override fun showComposition(composition: Composition) {
        headerItem.runAction { binding ->
            binding.tvCompositionName.text = composition.title

            val sb = DescriptionSpannableStringBuilder(this)
            sb.append(FormatUtils.formatAuthor(composition.artist, this))
            sb.append(FormatUtils.formatMilliseconds(composition.duration))
            sb.append(FormatUtils.formatSize(this, composition.size))
            binding.tvCompositionInfo.text = sb
        }
    }

    override fun showCompositionError(errorCommand: ErrorCommand) {
        headerItem.runAction { binding ->
            binding.tvCompositionName.text = errorCommand.message
            binding.tvCompositionInfo.text = null
        }
    }

    override fun shareComposition(composition: Composition) {
        shareComposition(this, supportFragmentManager, composition)
    }

    override fun showConfirmDeleteDialog(composition: Composition) {
        showConfirmDeleteDialog(this, listOf(composition)) {
            presenter.onDeleteCompositionsDialogConfirmed(composition)
        }
    }

    override fun closeScreen() {
        finish()
    }

    override fun showDeleteCompositionMessage(composition: DeletedComposition) {
        val text = MessagesUtils.getDeleteCompleteMessage(this, listOf(composition))
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    override fun showDeleteCompositionError(errorCommand: ErrorCommand) {
        deletingErrorHandler.handleError(errorCommand) {
            Toast.makeText(this, errorCommand.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onMenuItemClicked(menuItem: MenuItem) {
        when(menuItem.itemId) {
            R.id.menu_share -> presenter.onShareCompositionClicked()
            R.id.menu_delete -> presenter.onDeleteCompositionClicked()
        }
    }

    private fun applyCompositionId(intent: Intent) {
        val id = intent.getLongExtra(Arguments.ID_ARG, 0)
        presenter.onCompositionIdReceived(id)
    }

    private fun showEditorRequestDeniedMessage() {
        Toast.makeText(
            this,
            R.string.android_r_edit_file_permission_denied,
            Toast.LENGTH_LONG
        ).show()
    }

}