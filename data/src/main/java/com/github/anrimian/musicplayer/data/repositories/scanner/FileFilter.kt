package com.github.anrimian.musicplayer.data.repositories.scanner

import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository

open class FileFilter(private val settingsRepository: SettingsRepository) {

    open fun isFileExtensionAllowed(fileName: String): Boolean {
        val ext = fileName.substringAfterLast('.', "")
        val extensions = settingsRepository.getAllowedFileExtensions()
        return extensions.contains(ext)
    }

}