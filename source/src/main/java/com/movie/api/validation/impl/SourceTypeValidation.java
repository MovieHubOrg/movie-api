package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.SourceTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class SourceTypeValidation implements ConstraintValidator<SourceTypeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(SourceTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.SOURCE_TYPE_INTERNAL)
                || Objects.equals(value, BaseConstant.SOURCE_TYPE_EXTERNAL);
    }
}