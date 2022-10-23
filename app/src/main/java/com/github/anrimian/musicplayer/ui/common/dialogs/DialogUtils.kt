package com.github.anrimian.musicplayer.ui.common.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSoundBalanceBinding
import com.github.anrimian.musicplayer.databinding.PartialDeleteDialogBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.dialogs.share.newShareCompositionsDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

fun showSoundBalanceSelectorDialog(
    context: Context,
    balance: SoundBalance,
    onBalancePicked: (SoundBalance) -> Unit,
    onBalanceSelected: (SoundBalance) -> Unit,
    onReset: () -> Unit,
) {

    val viewBinding = DialogSoundBalanceBinding.inflate(LayoutInflater.from(context))
    viewBinding.sbSoundBalance.max = 200
    val seekBarViewWrapper = SeekBarViewWrapper(viewBinding.sbSoundBalance)
    seekBarViewWrapper.setProgressChangeListener { progress ->
        val left = if (progress < 100) progress else 100
        val right = if (progress > 100) 100 - (progress - 100) else 100
        viewBinding.tvLeftValue.text = context.getString(R.string.percent_template, left)
        viewBinding.tvRightValue.text = context.getString(R.string.percent_template, right)
        onBalancePicked(SoundBalance(left/100f, right/100f))
    }
    seekBarViewWrapper.setOnSeekStopListener { progress ->
        val left = if (progress < 100) progress else 100
        val right = if (progress > 100) 100 - (progress - 100) else 100
        onBalanceSelected(SoundBalance(left/100f, right/100f))
    }
    val balanceLeft = (balance.left * 100).toLong()
    val balanceRight = (balance.right * 100).toLong()
    val progress = if (balanceLeft < 100) balanceLeft else balanceLeft + (100 - balanceRight)
    seekBarViewWrapper.setProgress(progress)
    viewBinding.tvLeftValue.text = context.getString(R.string.percent_template, balanceLeft)
    viewBinding.tvRightValue.text = context.getString(R.string.percent_template, balanceRight)

    AlertDialog.Builder(context)
        .setTitle(R.string.sound_balance)
        .setView(viewBinding.root)
        .setPositiveButton(android.R.string.ok) { _, _ -> }
        .setNegativeButton(R.string.reset) { _, _ -> onReset()}
        .show()
}

fun shareComposition(fragment: Fragment, composition: Composition) {
    shareCompositions(fragment, listOf(composition))
}

fun shareCompositions(fragment: Fragment, compositions: Collection<Composition>) {
    val ids = compositions.map(Composition::getId)

    val nonExistentComposition = compositions.find { composition -> !composition.isFileExists }
    if (nonExistentComposition != null) {
        newShareCompositionsDialogFragment(ids.toLongArray()).safeShow(fragment.childFragmentManager)
        return
    }
    val ctx = fragment.requireContext()
    Components.getAppComponent().sourceInteractor()
        .getLibraryCompositionSources(ids)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ uris -> launchShareSourcesActivity(ctx, uris)}, { t -> showShareErrorMessage(ctx, t) })
}

fun showShareErrorMessage(context: Context, throwable: Throwable) {
    val errorCommand = Components.getAppComponent().errorParser().parseError(throwable)
    Toast.makeText(context, errorCommand.message, Toast.LENGTH_LONG).show()
}

fun launchShareSourcesActivity(context: Context, sources: ArrayList<CompositionContentSource>) {
    val uriBuilder = Components.getAppComponent().contentSourceHelper()
    val uris = sources.mapTo(ArrayList(sources.size), uriBuilder::createUri)
    launchShareActivity(context, uris)
}

fun launchShareActivity(context: Context, uris: ArrayList<Uri>) {
    val sbTitle = StringBuilder(context.getString(R.string.share))

    val intent = if (uris.size == 1) {
        Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uris[0])
        }
    } else {
        sbTitle.append(" (")
        sbTitle.append(
            context.resources.getQuantityString(R.plurals.files_count, uris.size, uris.size)
        )
        sbTitle.append(")")
        Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }
    }
    intent.type = "audio/*"
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(Intent.createChooser(intent, sbTitle.toString()))
}

fun showConfirmDeleteDialog(
    context: Context,
    compositions: List<Composition>,
    deleteCallback: () -> Unit,
) {
    val count = compositions.size
    val countMessage = if (count == 1) {
        context.getString(
            R.string.delete_composition_template,
            CompositionHelper.formatCompositionName(compositions[0])
        )
    } else {
        getDeleteCompositionsMessage(context, count)
    }
    val message = context.getString(R.string.undone_action_template, countMessage)
    val hasExistingFiles = compositions.find { composition ->
        composition.isFileExists && composition.initialSource == InitialSource.LOCAL
    } != null
    showConfirmDeleteFileDialog(context, message, deleteCallback, hasExistingFiles)
}

fun showConfirmDeleteDialog(
    context: Context,
    folder: FolderFileSource,
    deleteCallback: () -> Unit,
) {
    val filesCount = folder.filesCount
    val name = folder.name
    val countMessage = if (filesCount == 0) {
        context.getString(R.string.delete_empty_folder, name)
    } else {
        context.getString(
            R.string.delete_folder_template,
            name,
            getDeleteCompositionsMessage(context, filesCount)
        )
    }
    val message = context.getString(R.string.undone_action_template, countMessage)
    showConfirmDeleteFileDialog(context, message, deleteCallback, folder.hasAnyStorageFile())
}

fun showConfirmDeleteDialog(
    context: Context,
    playList: PlayList,
    deleteCallback: () -> Unit,
) {
    val message = context.getString(R.string.delete_playlist_template, playList.name)
    showConfirmDeleteDialog(context, message, deleteCallback)
}

fun showConfirmDeleteFileDialog(
    context: Context,
    message: String,
    deleteCallback: () -> Unit,
    hasStorageFiles: Boolean
) {
    var view: View? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && hasStorageFiles) {

        val interactor = Components.getAppComponent().librarySettingsInteractor()
        val isConfirmDialogEnabled = interactor.isAppConfirmDeleteDialogEnabled()

        if (!isConfirmDialogEnabled) {
            deleteCallback()
            return
        }
        val binding = PartialDeleteDialogBinding.inflate(LayoutInflater.from(context))
        setChecked(binding.cbDoNotShowDeleteDialog, !isConfirmDialogEnabled)
        onCheckChanged(binding.cbDoNotShowDeleteDialog) { enabled ->
            interactor.setAppConfirmDeleteDialogEnabled(!enabled)
        }
        view = binding.root
    }
    showConfirmDeleteDialog(context, message, deleteCallback, view)
}

fun showConfirmDeleteDialog(
    context: Context,
    message: String,
    deleteCallback: () -> Unit,
    view: View? = null
) {
    AlertDialog.Builder(context)
        .setTitle(R.string.deleting)
        .setMessage(message)
        .apply { if (view != null) setView(view) }
        .setPositiveButton(R.string.delete) { _, _ -> deleteCallback() }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
}

private fun getDeleteCompositionsMessage(context: Context, count: Int): String {
    return context.resources.getQuantityString(R.plurals.delete_compositions_template, count, count)
}