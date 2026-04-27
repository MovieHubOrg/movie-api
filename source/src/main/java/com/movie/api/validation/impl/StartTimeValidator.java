package com.movie.api.validation.impl;

import com.movie.api.form.room.CreateRoomForm;
import com.movie.api.validation.ValidStartTime;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Date;

public class StartTimeValidator implements ConstraintValidator<ValidStartTime, CreateRoomForm> {
    @Override
    public boolean isValid(CreateRoomForm form, ConstraintValidatorContext context) {
        if (Boolean.TRUE.equals(form.getIsStartNow())) return true;

        if (form.getStartTime() == null) {
            buildViolation(context, "startTime is required");
            return false;
        }

        if (!form.getStartTime().after(new Date())) {
            buildViolation(context, "startTime must be in the future");
            return false;
        }

        return true;
    }

    private void buildViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("startTime")
                .addConstraintViolation();
    }
}
