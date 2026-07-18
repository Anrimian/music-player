package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper

open class MessageTextFormatter {

    open fun getConfirmDeleteCompositionsText(
        context: Context,
        compositions: List<Composition>
    ): String {
        val warningMessage = context.getString(R.string.undone_action_template)
        return getCompositionsCountMessage(context, compositions, warningMessage)
    }

    @Composable
    @ReadOnlyComposable
    open fun getConfirmDeleteCompositionsText(
        count: Int,
        firstItemName: String?
    ): String {
        val warningMessage = stringResource(R.string.undone_action_template)
        return getCompositionsCountMessage(count, firstItemName, warningMessage)
    }

    open fun getConfirmDeleteFoldersText(context: Context, folder: FolderFileSource): String {
        val warningMessage = context.getString(R.string.undone_action_template)
        return getFolderCountMessage(context, folder, warningMessage)
    }

    @StringRes
    open fun getMissingFilesDescriptionResId(): Int {
        return R.string.missing_files_description
    }

    open fun getConfirmDeleteMissingCompositionsText(context: Context, count: Int): String {
        val warningMessage = context.getString(R.string.undone_action_template)
        return context.resources.getQuantityString(
            R.plurals.delete_compositions_from_library_question,
            count,
            count,
            warningMessage
        )
    }

    @Composable
    @ReadOnlyComposable
    protected fun getCompositionsCountMessage(
        count: Int,
        firstItemName: String?,
        warningMessage: String
    ): String {
        val question = if (firstItemName != null) {
            stringResource(
                R.string.delete_composition_template,
                firstItemName
            )
        } else {
            getDeleteCompositionsMessage(count)
        }
        return stringResource(
            R.string.question_template,
            question,
            warningMessage
        )
    }

    protected fun getCompositionsCountMessage(
        context: Context,
        compositions: List<Composition>,
        warningMessage: String
    ): String {
        val count = compositions.size
        val question = if (count == 1) {
            context.getString(
                R.string.delete_composition_template,
                CompositionHelper.formatCompositionName(compositions[0])
            )
        } else {
            getDeleteCompositionsMessage(context, count)
        }
        return context.getString(
            R.string.question_template,
            question,
            warningMessage
        )
    }

    protected fun getFolderCountMessage(
        context: Context,
        folder: FolderFileSource,
        warningMessage: String,
    ): String {
        val filesCount = folder.filesCount
        val name = folder.name
        val question = if (filesCount == 0) {
            context.getString(R.string.delete_empty_folder, name)
        } else {
            context.getString(
                R.string.delete_folder_template,
                name,
                getDeleteCompositionsMessage(context, filesCount)
            )
        }
        return context.getString(
            R.string.question_template,
            question,
            warningMessage
        )
    }

    @Composable
    @ReadOnlyComposable
    private fun getDeleteCompositionsMessage(count: Int): String {
        return pluralStringResource(R.plurals.delete_song_template, count, count)
    }

    private fun getDeleteCompositionsMessage(context: Context, count: Int): String {
        return context.resources.getQuantityString(R.plurals.delete_song_template, count, count)
    }

}