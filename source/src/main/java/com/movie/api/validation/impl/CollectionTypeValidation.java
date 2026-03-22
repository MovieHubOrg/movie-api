package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.CollectionTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class CollectionTypeValidation implements ConstraintValidator<CollectionTypeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(CollectionTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.COLLECTION_TYPE_TOPIC)
                || Objects.equals(value, BaseConstant.COLLECTION_TYPE_SECTION);
    }
}