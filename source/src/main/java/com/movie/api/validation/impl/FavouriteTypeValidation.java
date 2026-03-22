package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.FavouriteTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class FavouriteTypeValidation implements ConstraintValidator<FavouriteTypeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(FavouriteTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.FAVOURITE_TYPE_MOVIE)
                || Objects.equals(value, BaseConstant.FAVOURITE_TYPE_PERSON);
    }
}