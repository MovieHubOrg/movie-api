package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.MovieItemKindConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class MovieItemKindValidation implements ConstraintValidator<MovieItemKindConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(MovieItemKindConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.MOVIE_ITEM_KIND_SEASON)
                || Objects.equals(value, BaseConstant.MOVIE_ITEM_KIND_EPISODE)
                || Objects.equals(value, BaseConstant.MOVIE_ITEM_KIND_TRAILER);
    }
}