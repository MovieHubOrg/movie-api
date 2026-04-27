package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.RoomKindConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class RoomKindValidation implements ConstraintValidator<RoomKindConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(RoomKindConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.ROOM_KIND_PRIVATE)
                || Objects.equals(value, BaseConstant.ROOM_KIND_PUBLIC);
    }
}
