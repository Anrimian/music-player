package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.error.ErrorType;

public interface PlayerErrorParser {

    ErrorType getErrorType(Throwable throwable);
}
