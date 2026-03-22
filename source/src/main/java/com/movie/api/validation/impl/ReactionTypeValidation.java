package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.ReactionTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class ReactionTypeValidation implements ConstraintValidator<ReactionTypeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(ReactionTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.REACTION_TYPE_LIKE)
                || Objects.equals(value, BaseConstant.REACTION_TYPE_DISLIKE);
    }
}