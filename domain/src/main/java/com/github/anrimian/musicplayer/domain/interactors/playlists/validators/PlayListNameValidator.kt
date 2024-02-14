package com.github.anrimian.musicplayer.domain.interactors.playlists.validators

import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.domain.utils.validation.Cause
import com.github.anrimian.musicplayer.domain.utils.validation.SingleValidator
import com.github.anrimian.musicplayer.domain.utils.validation.ValidateError

class PlayListNameValidator : SingleValidator<String>() {

    override fun processValidation(model: String): ValidateError? {
        if (TextUtils.isEmpty(model)) {
            return ValidateError(Cause.EMPTY_NAME)
        }
        if (!PlayListFileNameValidator.isPlaylistNameNotTooLong(model)) {
            return ValidateError(Cause.TOO_LONG_NAME)
        }
        return null
    }
}