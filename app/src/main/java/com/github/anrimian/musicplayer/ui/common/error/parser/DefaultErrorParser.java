package com.github.anrimian.musicplayer.ui.common.error.parser;

import android.app.RecoverableSecurityException;
import android.content.Context;
import android.os.Build;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.internal.EqInitializationException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyDeletedException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListAlreadyExistsException;
import com.github.anrimian.musicplayer.data.models.exceptions.PlayListNotCreatedException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.DuplicateFolderNamesException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.EditorTimeoutException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.FileExistsException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveFolderToItselfException;
import com.github.anrimian.musicplayer.data.repositories.library.edit.exceptions.MoveInTheSameFolderException;
import com.github.anrimian.musicplayer.data.storage.exceptions.NotAllowedPathException;
import com.github.anrimian.musicplayer.data.storage.exceptions.UnavailableMediaStoreException;
import com.github.anrimian.musicplayer.data.storage.providers.music.RecoverableSecurityExceptionExt;
import com.github.anrimian.musicplayer.domain.interactors.analytics.Analytics;
import com.github.anrimian.musicplayer.domain.models.composition.content.LocalSourceNotFoundException;
import com.github.anrimian.musicplayer.domain.models.composition.content.UnsupportedSourceException;
import com.github.anrimian.musicplayer.domain.models.exceptions.EditorReadException;
import com.github.anrimian.musicplayer.domain.models.exceptions.FileWriteNotAllowedException;
import com.github.anrimian.musicplayer.domain.models.exceptions.FolderAlreadyIgnoredException;
import com.github.anrimian.musicplayer.domain.models.exceptions.StorageTimeoutException;
import com.github.anrimian.musicplayer.domain.utils.validation.ValidateError;
import com.github.anrimian.musicplayer.domain.utils.validation.ValidateException;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.editor.common.EditorErrorCommand;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created on 29.10.2017.
 */

public class DefaultErrorParser extends ErrorParser {

    private final Analytics analytics;

    public DefaultErrorParser(Context context, Analytics analytics) {
        super(context);
        this.analytics = analytics;
    }

    @Override
    public ErrorCommand parseError(Throwable throwable) {
        if (throwable instanceof ValidateException) {
            ValidateException exception = (ValidateException) throwable;
            List<ValidateError> validateErrors = exception.getValidateErrors();
            for (ValidateError validateError: validateErrors) {
                switch (validateError.getCause()) {
                    case EMPTY_NAME: {
                        return error(R.string.name_can_not_be_empty);
                    }
                }
            }
        }
        if (throwable instanceof PlayListNotCreatedException
                || throwable instanceof PlayListAlreadyExistsException) {
            return error(R.string.play_list_with_this_name_already_exists);
        }
        if (throwable instanceof PlayListAlreadyDeletedException) {
            return error(R.string.play_not_exists);
        }
        if (throwable instanceof FileNotFoundException || throwable instanceof LocalSourceNotFoundException) {
            return error(R.string.file_not_found);
        }
        if (throwable instanceof MoveInTheSameFolderException) {
            return error(R.string.move_in_the_same_folder_error);
        }
        if (throwable instanceof MoveFolderToItselfException) {
            return error(R.string.moving_and_destination_folders_matches);
        }
        if (throwable instanceof DuplicateFolderNamesException) {
            return error(R.string.folder_with_this_name_already_exists_in_dest_folder);
        }
        if (throwable instanceof FileExistsException) {
            return error(R.string.file_already_exists);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
                && (throwable instanceof RecoverableSecurityException || throwable instanceof RecoverableSecurityExceptionExt)) {
            return new EditorErrorCommand(throwable);
        }
        if (throwable instanceof EqInitializationException) {
            return error(R.string.equalizer_initialization_error);
        }
        if (throwable instanceof EditorTimeoutException) {
            return error(R.string.editor_timeout_error);
        }
        if (throwable instanceof EditorReadException) {
            return new ErrorCommand(throwable.getMessage());
        }
        if (throwable instanceof NullPointerException) {
            logException(throwable);
            return error(R.string.internal_app_error);
        }
        if (throwable instanceof StorageTimeoutException) {
            return error(R.string.storage_timeout_error_message);
        }
        if (throwable instanceof UnavailableMediaStoreException) {
            return error(R.string.system_media_store_system_error);
        }
        if (throwable instanceof UnsupportedSourceException) {
            return error(R.string.unsupported_format_hint);
        }
        if (throwable instanceof FolderAlreadyIgnoredException) {
            return error(R.string.folder_already_excluded_from_scanning);
        }
        if (throwable instanceof NotAllowedPathException) {
            return new ErrorCommand(
                    getString(R.string.android_r_editor_restriction_error, throwable.getMessage())
            );
        }
        if (throwable instanceof FileWriteNotAllowedException) {
            logException(throwable);
            return error(R.string.write_to_this_is_not_allowed);
        }
        logException(throwable);
        return new ErrorCommand(getString(R.string.unexpected_error, throwable.getMessage()));
    }

    @Override
    public void logError(Throwable throwable) {
        logException(throwable);
    }

    private void logException(Throwable throwable) {
        analytics.processNonFatalError(throwable);
    }

}
