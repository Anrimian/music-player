package com.github.anrimian.simplemusicplayer.utils.error.parser;

import com.github.anrimian.simplemusicplayer.utils.error.ErrorCommand;

/**
 * Created on 29.10.2017.
 */

public interface ErrorParser<T extends ErrorCommand> {

    T parseError(Throwable throwable);
}
