package com.github.anrimian.simplemusicplayer.domain.business.playlists.validators;

import com.github.anrimian.simplemusicplayer.domain.utils.TextUtils;
import com.github.anrimian.simplemusicplayer.domain.utils.validation.Cause;
import com.github.anrimian.simplemusicplayer.domain.utils.validation.SingleValidator;
import com.github.anrimian.simplemusicplayer.domain.utils.validation.ValidateError;

import javax.annotation.Nullable;

public class PlayListNameValidator extends SingleValidator<String> {

    @Nullable
    @Override
    protected ValidateError processValidation(String model) {
        if (TextUtils.isEmpty(model)) {
            return new ValidateError(Cause.EMPTY_NAME);
        }
        return null;
    }
}
