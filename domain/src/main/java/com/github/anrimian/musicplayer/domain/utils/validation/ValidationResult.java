package com.github.anrimian.musicplayer.domain.utils.validation;

import java.util.List;

class ValidationResult<T> {

    private final T model;
    private final List<ValidateError> validateErrors;

    ValidationResult(T model, List<ValidateError> validateErrors) {
        this.model = model;
        this.validateErrors = validateErrors;
    }

    T getModel() {
        return model;
    }

    List<ValidateError> getValidateErrors() {
        return validateErrors;
    }
}
