package com.github.anrimian.musicplayer.utils.logger;

import com.github.anrimian.musicplayer.domain.repositories.LoggerRepository;

public class AppLogger {

    private final FileLog fileLog;
    private final LoggerRepository loggerRepository;

    public AppLogger(FileLog fileLog, LoggerRepository loggerRepository) {
        this.fileLog = fileLog;
        this.loggerRepository = loggerRepository;
    }

    public void initFatalErrorRecorder() {
        Thread.UncaughtExceptionHandler handler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(
                (thread, e) -> {
                    loggerRepository.setWasFatalError(true);
                    fileLog.writeFatalException(e);
                    if (handler != null) {
                        handler.uncaughtException(thread, e);
                    }
                });
    }
}
