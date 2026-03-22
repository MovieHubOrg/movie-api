package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.GenderConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class GenderValidation implements ConstraintValidator<GenderConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(GenderConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.GENDER_MALE)
                || Objects.equals(value, BaseConstant.GENDER_FEMALE)
                || Objects.equals(value, BaseConstant.GENDER_OTHER);
    }
}