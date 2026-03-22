package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.AgeRatingConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

public class AgeRatingValidation implements ConstraintValidator<AgeRatingConstraint, Integer> {
    private boolean allowNull;

    public Set<Integer> VALID_AGE_RATINGS = Set.of(
            BaseConstant.AGE_RATING_GENERAL,
            BaseConstant.AGE_RATING_PG,
            BaseConstant.AGE_RATING_PG13,
            BaseConstant.AGE_RATING_R,
            BaseConstant.AGE_RATING_NC17,
            BaseConstant.AGE_RATING_18_PLUS
    );

    @Override
    public void initialize(AgeRatingConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return VALID_AGE_RATINGS.contains(value);
    }
}