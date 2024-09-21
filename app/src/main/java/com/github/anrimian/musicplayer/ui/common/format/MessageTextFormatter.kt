package com.github.anrimian.musicplayer.ui.common.format

import android.content.Context
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.folders.FolderFileSource
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper

open class MessageTextFormatter {

    open fun getConfirmDeleteCompositionsText(
        context: Context,
        compositions: List<Composition>
    ): String {
        val countMessage = getCompositionsCountMessage(context, compositions)
        return context.getString(R.string.undone_action_template, countMessage)
    }

    open fun getConfirmDeleteFoldersText(context: Context, folder: FolderFileSource): String {
        val countMessage = getFolderCountMessage(context, folder)
        return context.getString(R.string.undone_action_template, countMessage)
    }

    protected fun getCompositionsCountMessage(
        context: Context,
        compositions: List<Composition>
    ): String {
        val count = compositions.size
        return if (count == 1) {
            context.getString(
                R.string.delete_composition_template,
                CompositionHelper.formatCompositionName(compositions[0])
            )
        } else {
            getDeleteCompositionsMessage(context, count)
        }
    }

    protected fun getFolderCountMessage(context: Context, folder: FolderFileSource): String {
        val filesCount = folder.filesCount
        val name = folder.name
        return if (filesCount == 0) {
            context.getString(R.string.delete_empty_folder, name)
        } else {
            context.getString(
                R.string.delete_folder_template,
                name,
                getDeleteCompositionsMessage(context, filesCount)
            )
        }
    }

    private fun getDeleteCompositionsMessage(context: Context, count: Int): String {
        return context.resources.getQuantityString(R.plurals.delete_compositions_template, count, count)
    }

}