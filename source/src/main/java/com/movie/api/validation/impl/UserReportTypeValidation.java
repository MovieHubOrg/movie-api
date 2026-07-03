package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.UserReportTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class UserReportTypeValidation implements ConstraintValidator<UserReportTypeConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(UserReportTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.USER_REPORT_TYPE_COMMENT)
                || Objects.equals(value, BaseConstant.USER_REPORT_TYPE_REVIEW)
                || Objects.equals(value, BaseConstant.USER_REPORT_TYPE_VIDEO);
    }
}
