package com.github.anrimian.data

import com.github.anrimian.common.WearableFields
import com.github.anrimian.domain.models.ExternalWearableComposition
import com.github.anrimian.domain.models.LibraryWearableComposition
import com.github.anrimian.domain.models.WearableComposition
import com.github.anrimian.domain.models.WearablePlayQueueItem
import com.github.anrimian.musicplayer.data.utils.preferences.SharedPreferencesHelper
import com.github.anrimian.musicplayer.domain.utils.NumberUtils
import org.json.JSONObject

//TODO-W place it here? split serializer-writer_to_prefs-analyzer?
object WearableModelsHelper {

    fun writeComposition(source: WearableComposition?, preferences: SharedPreferencesHelper) {
        val editor = preferences.edit()
        if (source == null) {
            editor.remove(WearableFields.ID)
                .remove(WearableFields.TITLE)
                .remove(WearableFields.ARTIST)
                .remove(WearableFields.DURATION)
        } else {
            if (source is LibraryWearableComposition) {
                editor.putLong(WearableFields.ID, source.id)
            } else {
                editor.remove(WearableFields.ID)
            }
            editor.putString(WearableFields.TITLE, source.title)
                .putString(WearableFields.ARTIST, source.artist)
                .putLong(WearableFields.DURATION, source.duration)
        }
        editor.apply()
    }

    fun readComposition(preferences: SharedPreferencesHelper): WearableComposition? {
        val title = preferences.getString(WearableFields.TITLE)
        if (title == null) {
            return null
        }
        val artist = preferences.getString(WearableFields.ARTIST)
        val duration = preferences.getLong(WearableFields.DURATION)
        val id = preferences.getLong(WearableFields.ID)
        if (id == 0L) {
            return ExternalWearableComposition(title, artist, duration)
        }
        return LibraryWearableComposition(id, title, artist, duration)
    }

    fun serializePlayState(isPlaying: Boolean, updateTime: Long): ByteArray {
        val array = ByteArray(1 + Long.SIZE_BYTES)
        NumberUtils.booleanToBytes(isPlaying, array)
        NumberUtils.longToBytes(updateTime, array, 1)
        return array
    }

    fun serializeComposition(source: WearableComposition?): JSONObject {
        val jsonObject = JSONObject()
        if (source != null) {
            if (source is LibraryWearableComposition) {
                jsonObject.put(WearableFields.ID, source.id)
            }
            jsonObject.put(WearableFields.TITLE, source.title)
            jsonObject.put(WearableFields.ARTIST, source.artist)
            jsonObject.put(WearableFields.DURATION, source.duration)
        }
        return jsonObject
    }

    @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
    fun deserializeComposition(jsonObject: JSONObject): WearableComposition? {
        val title = jsonObject.optString(WearableFields.TITLE, null) as String?
        if (title == null) {
            return null
        }
        val artist = jsonObject.optString(WearableFields.ARTIST, null)
        val duration = jsonObject.getLong(WearableFields.DURATION)
        val id = jsonObject.optLong(WearableFields.ID, -1L)
        if (id == -1L) {
            return ExternalWearableComposition(title, artist, duration)
        }
        return LibraryWearableComposition(id, title, artist, duration)
    }

    fun areContentsTheSame(first: WearablePlayQueueItem?, second: WearablePlayQueueItem?): Boolean {
        if (first === second) {
            return true
        }
        if (first == null || second == null) {
            return false
        }
        return first.id == second.id
                && first.title == second.title
                && first.artist == second.artist
                && first.duration == second.duration
    }

    fun areContentsTheSame(first: WearableComposition?, second: WearableComposition?): Boolean {
        if (first == null || second == null) {
            return false
        }
        if (first.javaClass != second.javaClass) {
            return false
        }
        if (first is LibraryWearableComposition) {
            return areContentsTheSame(first, second as LibraryWearableComposition)
        }
        return areContentsTheSame(
            first as ExternalWearableComposition,
            second as ExternalWearableComposition
        )
    }

    fun areContentsTheSame(
        first: LibraryWearableComposition?,
        second: LibraryWearableComposition?,
    ): Boolean {
        if (first === second) {
            return true
        }
        if (first == null || second == null) {
            return false
        }
        return first.id == second.id
                && first.title == second.title
                && first.artist == second.artist
                && first.duration == second.duration
    }

    fun areContentsTheSame(
        first: ExternalWearableComposition?,
        second: ExternalWearableComposition?,
    ): Boolean {
        if (first === second) {
            return true
        }
        if (first == null || second == null) {
            return false
        }
        return first.title == second.title
                && first.artist == second.artist
                && first.duration == second.duration
    }

}