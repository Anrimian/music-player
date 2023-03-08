package com.github.anrimian.musicplayer.ui.widgets

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.ContextCompat
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetColors
import com.github.anrimian.musicplayer.ui.widgets.models.WidgetData

object WidgetDataHolder {

    private const val WIDGET_STATE = "widget_state"
    private const val CURRENT_COMPOSITION_NAME = "current_composition"
    private const val CURRENT_COMPOSITION_AUTHOR = "current_composition_author"
    private const val CURRENT_QUEUE_SIZE = "current_queue_size"
    private const val CURRENT_COMPOSITION_ID = "current_composition_id"
    private const val CURRENT_COMPOSITION_UPDATE_TIME = "current_composition_update_time"
    private const val CURRENT_COMPOSITION_COVER_MODIFY_TIME = "current_composition_cover_modify_time"
    private const val CURRENT_COMPOSITION_SIZE = "current_composition_size"
    private const val CURRENT_COMPOSITION_IS_FILE_EXISTS = "current_composition_is_file_exists"
    private const val RANDOM_PLAY = "random_play"
    private const val REPEAT = "repeat"
    private const val COVERS_ENABLED = "covers_enabled"
    private const val COLOR_BACKGROUND = "color_background"
    private const val COLOR_ACCENT = "color_accent"
    private const val COLOR_BUTTON = "color_button"
    private const val COLOR_TEXT_PRIMARY = "color_text_primary"
    private const val COLOR_TEXT_SECONDARY = "color_text_secondary"
    private const val USE_DEFAULT_COLORS = "use_default_colors"

    private var widgetData: WidgetData? = null

    fun getWidgetData(context: Context): WidgetData {
        return widgetData ?: loadWidgetData(context).also { data -> widgetData = data }
    }

    fun applyWidgetData(
        context: Context,
        compositionName: String?,
        compositionAuthor: String?,
        compositionId: Long,
        compositionUpdateTime: Long,
        compositionCoverModifyTime: Long,
        compositionSize: Long,
        isFileExists: Boolean,
        queueSize: Int,
        playerState: Int,
        randomPlayModeEnabled: Boolean,
        repeatMode: Int,
        isCoversEnabled: Boolean,
    ): Boolean {
        val widgetData = getWidgetData(context)

        val preferences = getWidgetPreferences(context)
        val editor = preferences.edit()

        var savableDataChanged = false
        var dataChanged = false
        if (widgetData.compositionName != compositionName) {
            editor.putString(CURRENT_COMPOSITION_NAME, compositionName)
            widgetData.compositionName = compositionName
            savableDataChanged = true
        }
        if (widgetData.compositionAuthor != compositionAuthor) {
            editor.putString(CURRENT_COMPOSITION_AUTHOR, compositionAuthor)
            widgetData.compositionAuthor = compositionAuthor
            savableDataChanged = true
        }
        if (widgetData.compositionId != compositionId) {
            editor.putLong(CURRENT_COMPOSITION_ID, compositionId)
            widgetData.compositionId = compositionId
            savableDataChanged = true
        }
        if (widgetData.compositionUpdateTime != compositionUpdateTime) {
            editor.putLong(CURRENT_COMPOSITION_UPDATE_TIME, compositionUpdateTime)
            widgetData.compositionUpdateTime = compositionUpdateTime
            savableDataChanged = true
        }
        if (widgetData.coverModifyTime != compositionCoverModifyTime) {
            editor.putLong(CURRENT_COMPOSITION_COVER_MODIFY_TIME, compositionCoverModifyTime)
            widgetData.coverModifyTime = compositionCoverModifyTime
            savableDataChanged = true
        }
        if (widgetData.compositionSize != compositionSize) {
            editor.putLong(CURRENT_COMPOSITION_SIZE, compositionSize)
            widgetData.compositionSize = compositionSize
            savableDataChanged = true
        }
        if (widgetData.isFileExists != isFileExists) {
            editor.putBoolean(CURRENT_COMPOSITION_IS_FILE_EXISTS, isFileExists)
            widgetData.isFileExists = isFileExists
            savableDataChanged = true
        }
        if (widgetData.queueSize != queueSize) {
            editor.putInt(CURRENT_QUEUE_SIZE, queueSize)
            widgetData.queueSize = queueSize
            savableDataChanged = true
        }
        if (widgetData.playerState != playerState) {
            widgetData.playerState = playerState
            dataChanged = true
        }
        if (widgetData.randomPlayModeEnabled != randomPlayModeEnabled) {
            editor.putBoolean(RANDOM_PLAY, randomPlayModeEnabled)
            widgetData.randomPlayModeEnabled = randomPlayModeEnabled
            savableDataChanged = true
        }
        if (widgetData.repeatMode != repeatMode) {
            editor.putInt(REPEAT, repeatMode)
            widgetData.repeatMode = repeatMode
            savableDataChanged = true
        }
        if (widgetData.isCoversEnabled != isCoversEnabled) {
            editor.putBoolean(COVERS_ENABLED, isCoversEnabled)
            widgetData.isCoversEnabled = isCoversEnabled
            savableDataChanged = true
        }

        if (savableDataChanged) {
            editor.apply()
        }
        return dataChanged || savableDataChanged
    }

    fun getWidgetColors(context: Context): WidgetColors? {
        val preferences = getPreferences(context)
        if (preferences.getBoolean(USE_DEFAULT_COLORS, true)) {
            return null
        }
        val backgroundColor = preferences.getInt(
            COLOR_BACKGROUND,
            ContextCompat.getColor(context, R.color.widget_default_bg_color)
        )
        val colorRes = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) android.R.color.system_accent1_500 else R.color.colorAccent
        val accentColor = preferences.getInt(COLOR_ACCENT, ContextCompat.getColor(context, colorRes))
        val buttonColor = preferences.getInt(
            COLOR_BUTTON,
            ContextCompat.getColor(context, R.color.primary_button_color)
        )
        val primaryTextColor = preferences.getInt(
            COLOR_TEXT_PRIMARY,
            ContextCompat.getColor(context, R.color.text_color_primary)
        )
        val secondaryTextColor = preferences.getInt(
            COLOR_TEXT_SECONDARY,
            ContextCompat.getColor(context, R.color.text_color_secondary)
        )
        return WidgetColors(
            backgroundColor,
            accentColor,
            buttonColor,
            primaryTextColor,
            secondaryTextColor
        )
    }

    private fun getPreferences(context: Context): SharedPreferencesHelper {
        val preferences = getWidgetPreferences(context)
        return SharedPreferencesHelper(preferences)
    }

    private fun getWidgetPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(WIDGET_STATE, Context.MODE_PRIVATE)
    }

    private fun loadWidgetData(context: Context): WidgetData {
        val preferences = getPreferences(context)
        return WidgetData(
            preferences.getString(CURRENT_COMPOSITION_NAME),
            preferences.getString(CURRENT_COMPOSITION_AUTHOR),
            preferences.getLong(CURRENT_COMPOSITION_ID),
            preferences.getLong(CURRENT_COMPOSITION_UPDATE_TIME),
            preferences.getLong(CURRENT_COMPOSITION_COVER_MODIFY_TIME),
            preferences.getLong(CURRENT_COMPOSITION_SIZE),
            preferences.getBoolean(CURRENT_COMPOSITION_IS_FILE_EXISTS),
            preferences.getInt(CURRENT_QUEUE_SIZE),
            Constants.RemoteViewPlayerState.PAUSE,
            preferences.getBoolean(RANDOM_PLAY),
            preferences.getInt(REPEAT),
            preferences.getBoolean(COVERS_ENABLED),
        )
    }
}