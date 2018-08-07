package com.github.anrimian.simplemusicplayer.domain.utils.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import static java.util.Arrays.asList;

public abstract class SingleValidator<T> extends Validator<T> {

    @Override
    protected List<ValidateFunction> getValidateFunctions(T model) {
        return Collections.singletonList(() -> processValidation(model));
    }

    @Nullable
    protected abstract ValidateError processValidation(T model);
}
