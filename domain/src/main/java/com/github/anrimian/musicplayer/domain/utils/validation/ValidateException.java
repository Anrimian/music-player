package com.github.anrimian.musicplayer.domain.utils.validation;

import java.util.List;

public class ValidateException extends RuntimeException {

    private List<ValidateError> validateErrors;

    ValidateException(List<ValidateError> validateErrors) {
        this.validateErrors = validateErrors;
    }

    public List<ValidateError> getValidateErrors() {
        return validateErrors;
    }
}
