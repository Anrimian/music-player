package com.github.anrimian.musicplayer.ui.common.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogPlaylistDuplicateBinding
import com.github.anrimian.musicplayer.databinding.DialogSoundBalanceBinding
import com.github.anrimian.musicplayer.databinding.PartialDeleteDialogBinding
import com.github.anrimian.musicplayer.databinding.PartialNumberPickerDialogBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.dialogs.share.ShareCompositionsDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers

private const val MAX_DISPLAY_DUPLICATE_FILES_COUNT = 5

fun showPlaylistDuplicateEntryDialog(
    context: Context,
    compositions: Collection<Composition>,
    hasNonDuplicates: Boolean,
    playList: PlayList,
    isDuplicateCheckEnabled: Boolean,
    onAddEntriesConfirmed: (ignoreDuplicates: Boolean) -> Unit,
    onDuplicateChecked: (isChecked: Boolean) -> Unit
) {
    val binding = DialogPlaylistDuplicateBinding.inflate(LayoutInflater.from(context))

    binding.cbCheck.isChecked = isDuplicateCheckEnabled
    binding.cbCheck.setOnCheckedChangeListener { _, isChecked -> onDuplicateChecked(isChecked) }

    val dialog = AlertDialog.Builder(context)
        .setTitle(R.string.duplicates_detected)
        .setView(binding.root)
        .create()
    dialog.show()

    val message = context.getString(R.string.playlist_duplicates_description, playList.name)
    binding.tvMessage.text = message

    val duplicatesSb = StringBuilder(context.getString(R.string.compositions))
    duplicatesSb.append(':')
    for ((i, composition) in compositions.withIndex()) {
        if (i >= MAX_DISPLAY_DUPLICATE_FILES_COUNT) {
            duplicatesSb.append("\n")
            duplicatesSb.append(
                context.getString(R.string.more_template, compositions.count() - i)
            )
            break
        }
        duplicatesSb.append("\n  ")
        duplicatesSb.append(composition.title)
    }
    binding.tvDuplicates.text = duplicatesSb.toString()

    binding.btnAdd.setOnClickListener {
        onAddEntriesConfirmed(false)
        dialog.dismiss()
    }
    binding.btnAddWithoutDuplicates.isVisible = hasNonDuplicates
    binding.btnAddWithoutDuplicates.setOnClickListener {
        onAddEntriesConfirmed(true)
        dialog.dismiss()
    }
    binding.btnCancel.setOnClickListener {
        dialog.dismiss()
    }
}

fun showSoundBalanceSelectorDialog(
    context: Context,
    balance: SoundBalance,
    onBalancePicked: (SoundBalance) -> Unit,
    onBalanceSelected: (SoundBalance) -> Unit,
    onReset: () -> Unit,
) {

    val binding = DialogSoundBalanceBinding.inflate(LayoutInflater.from(context))
    binding.sbSoundBalance.max = 200
    val seekBarViewWrapper = SeekBarViewWrapper(binding.sbSoundBalance)
    seekBarViewWrapper.setProgressChangeListener { progress ->
        val left = if (progress < 100) progress else 100
        val right = if (progress > 100) 100 - (progress - 100) else 100
        binding.tvLeftValue.text = context.getString(R.string.percent_template, left)
        binding.tvRightValue.text = context.getString(R.string.percent_template, right)
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
    binding.tvLeftValue.text = context.getString(R.string.percent_template, balanceLeft)
    binding.tvRightValue.text = context.getString(R.string.percent_template, balanceRight)

    AlertDialog.Builder(context)
        .setTitle(R.string.sound_balance)
        .setView(binding.root)
        .setPositiveButton(android.R.string.ok) { _, _ -> }
        .setNegativeButton(R.string.reset) { _, _ -> onReset()}
        .show()
}

fun shareComposition(fragment: Fragment, composition: Composition) {
    shareCompositions(fragment, listOf(composition))
}

fun shareCompositions(fragment: Fragment, compositions: Collection<Composition>) {
    if (compositions.isEmpty()) {
        return
    }
    shareCompositions(
        fragment.requireContext(),
        fragment.childFragmentManager,
        compositions.map(Composition::id),
        compositions.find { composition -> !composition.isFileExists } != null
    )
}

fun shareComposition(
    ctx: Context,
    fragmentManager: FragmentManager,
    fullComposition: Composition,
) {
    shareCompositions(
        ctx,
        fragmentManager,
        listOf(fullComposition.id),
        fullComposition.storageId == null
    )
}

fun shareCompositions(
    ctx: Context,
    fragmentManager: FragmentManager,
    ids: List<Long>,
    hasNonExistComposition: Boolean,
) {
    if (hasNonExistComposition) {
        ShareCompositionsDialogFragment.newInstance(ids.toLongArray()).safeShow(fragmentManager)
        return
    }
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
    showConfirmDeleteFileDialog(context, message, deleteCallback, folder.hasAnyStorageFile)
}

fun showConfirmDeleteDialog(
    context: Context,
    playLists: Collection<PlayList>,
    deleteCallback: () -> Unit,
) {
    val count = playLists.size
    if (count == 1) {
        showConfirmDeleteDialog(context, playLists.first(), deleteCallback)
    } else {
        val message = context.resources.getQuantityString(
            R.plurals.delete_playlists_template,
            count,
            count
        )
        showConfirmDeleteDialog(context, message, deleteCallback)
    }
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
    hasStorageFiles: Boolean,
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
        ViewUtils.setChecked(binding.cbDoNotShowDeleteDialog, !isConfirmDialogEnabled)
        ViewUtils.onCheckChanged(binding.cbDoNotShowDeleteDialog) { enabled ->
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
    view: View? = null,
) {
    AlertDialog.Builder(context)
        .setTitle(R.string.deleting)
        .setMessage(message)
        .apply { if (view != null) setView(view) }
        .setPositiveButton(R.string.delete) { _, _ -> deleteCallback() }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
}

fun showNumberPickerDialog(
    context: Context,
    minValue: Long,
    maxValue: Long,
    currentValue: Long,
    stepValue: Long = 1,
    valueFormatter: ((Long) -> String)? = null,
    pickCallback: (Long) -> Unit,
) {
    val binding = PartialNumberPickerDialogBinding.inflate(LayoutInflater.from(context))

    val pickAction: () -> Unit
    if (stepValue != 1L && valueFormatter != null) {
        binding.numberPicker.minValue = 0
        val values = ArrayList<Long>()
        var v = minValue
        var index = 0
        var currentIndex = 0
        while (v <= maxValue) {
            if (v == currentValue) {
                currentIndex = index
            }
            values.add(v)
            v += stepValue
            index++
        }
        binding.numberPicker.maxValue = index - 1
        binding.numberPicker.value = currentIndex
        binding.numberPicker.displayedValues = Array(values.size) { i -> valueFormatter(values[i])}
        pickAction = { pickCallback(values[binding.numberPicker.value]) }
    } else {
        binding.numberPicker.minValue = minValue.toInt()
        binding.numberPicker.maxValue = maxValue.toInt()
        binding.numberPicker.value = currentValue.toInt()
        pickAction = { pickCallback(binding.numberPicker.value.toLong()) }
    }

    AlertDialog.Builder(context)
        .setView(binding.root)
        .setPositiveButton(android.R.string.ok) { _, _ -> pickAction() }
        .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
        .show()
}

private fun getDeleteCompositionsMessage(context: Context, count: Int): String {
    return context.resources.getQuantityString(R.plurals.delete_compositions_template, count, count)
}