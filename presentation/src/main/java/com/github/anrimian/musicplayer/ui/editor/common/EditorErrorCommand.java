package com.github.anrimian.musicplayer.ui.editor.common;

import android.app.RecoverableSecurityException;
import android.content.IntentSender;
import android.os.Build;

import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;

import javax.annotation.Nullable;

public class EditorErrorCommand extends ErrorCommand {

    @Nullable
    private IntentSender intentSender;

    public EditorErrorCommand(SecurityException securityException) {
        super("");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (securityException instanceof RecoverableSecurityException) {
                RecoverableSecurityException recoverableSecurityException = (RecoverableSecurityException) securityException;
                intentSender = recoverableSecurityException.getUserAction().getActionIntent().getIntentSender();
            }
        }
    }

    @Nullable
    public IntentSender getIntentSender() {
        return intentSender;
    }
}
