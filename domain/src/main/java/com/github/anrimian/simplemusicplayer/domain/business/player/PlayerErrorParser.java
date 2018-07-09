package com.github.anrimian.simplemusicplayer.domain.business.player;

import com.github.anrimian.simplemusicplayer.domain.models.player.error.ErrorType;

public interface PlayerErrorParser {

    ErrorType getErrorType(Throwable throwable);
}
