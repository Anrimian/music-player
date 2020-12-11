package com.github.anrimian.musicplayer.ui.editor.common;

import android.app.RecoverableSecurityException;
import android.content.IntentSender;
import android.os.Build;

import com.github.anrimian.musicplayer.data.storage.providers.music.RecoverableSecurityExceptionExt;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import javax.annotation.Nullable;

public class EditorErrorCommand extends ErrorCommand {

    @Nullable
    private IntentSender intentSender;

    public EditorErrorCommand(SecurityException securityException) {
        super(securityException.getMessage() == null? "" : securityException.getMessage());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (securityException instanceof RecoverableSecurityException) {
                RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) securityException;
                intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
                return;
            }
            if (securityException instanceof RecoverableSecurityExceptionExt) {
                RecoverableSecurityExceptionExt exception = (RecoverableSecurityExceptionExt) securityException;
                intentSender = exception.getPIntent().getIntentSender();
            }
        }
    }

    @Nullable
    public IntentSender getIntentSender() {
        return intentSender;
    }
}
