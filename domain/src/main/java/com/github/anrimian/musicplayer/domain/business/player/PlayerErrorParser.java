package com.github.anrimian.musicplayer.domain.business.player;

import com.github.anrimian.musicplayer.domain.models.player.error.ErrorType;

public interface PlayerErrorParser {

    ErrorType getErrorType(Throwable throwable);
}
