package com.github.anrimian.musicplayer.ui.editor.common;

import android.annotation.TargetApi;
import android.app.RecoverableSecurityException;
import android.content.IntentSender;
import android.os.Build;

import com.github.anrimian.musicplayer.data.storage.providers.music.RecoverableSecurityExceptionExt;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import javax.annotation.Nonnull;

@TargetApi(Build.VERSION_CODES.R)
public class EditorErrorCommand extends ErrorCommand {

    @Nonnull
    private final IntentSender intentSender;

    public EditorErrorCommand(Throwable throwable) {
        super(throwable.getMessage() == null? "" : throwable.getMessage());

        if (throwable instanceof RecoverableSecurityException) {
            RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) throwable;
            intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
            return;
        }
        if (throwable instanceof RecoverableSecurityExceptionExt) {
            RecoverableSecurityExceptionExt exception = (RecoverableSecurityExceptionExt) throwable;
            intentSender = exception.getPIntent().getIntentSender();
            return;
        }
        throw new IllegalStateException("unknown throwable type: " + throwable);
    }

    @Nonnull
    public IntentSender getIntentSender() {
        return intentSender;
    }
}
