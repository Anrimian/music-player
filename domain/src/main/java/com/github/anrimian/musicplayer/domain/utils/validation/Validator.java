package com.github.anrimian.musicplayer.domain.utils.validation;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import io.reactivex.Single;

public abstract class Validator<Model> {

    public Single<Model> validate(Model model) {
        return Single.just(model)
                .map(this::validateModel)
                .flatMap(this::processResult);
    }

    private Single<Model> processResult(ValidationResult<Model> validationResult) {
        return Single.create(subscriber -> {
            List<ValidateError> validateErrors = validationResult.getValidateErrors();
            if (validateErrors.isEmpty()) {
                subscriber.onSuccess(validationResult.getModel());
            } else {
                subscriber.onError(new ValidateException(validateErrors));
            }
        });
    }

    private ValidationResult<Model> validateModel(Model model) {
        List<ValidateError> errors = new LinkedList<>();
        for (ValidateFunction validateFunctions : getValidateFunctions(model)) {
            ValidateError error = validateFunctions.validate();
            if (error != null) {
                errors.add(error);
            }
        }
        return new ValidationResult<>(model, errors);
    }

    protected abstract List<ValidateFunction> getValidateFunctions(Model model);

    protected interface ValidateFunction {

        @Nullable
        ValidateError validate();
    }
}
