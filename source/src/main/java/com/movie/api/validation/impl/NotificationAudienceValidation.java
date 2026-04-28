package com.movie.api.validation.impl;

import com.movie.api.constant.BaseConstant;
import com.movie.api.validation.NotificationAudienceConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Objects;

public class NotificationAudienceValidation implements ConstraintValidator<NotificationAudienceConstraint, Integer> {
    private boolean allowNull;

    @Override
    public void initialize(NotificationAudienceConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null && allowNull) {
            return true;
        }
        return Objects.equals(value, BaseConstant.SEND_NOTIFICATION_FOR_ALL_USERS)
                || Objects.equals(value, BaseConstant.SEND_NOTIFICATION_FOR_INTERESTED_USERS);
    }
}
