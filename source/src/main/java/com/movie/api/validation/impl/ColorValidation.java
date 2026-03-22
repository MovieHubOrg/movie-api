package com.movie.api.validation.impl;

import com.movie.api.validation.ColorConstraint;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ColorValidation implements ConstraintValidator<ColorConstraint, String> {
    private boolean allowNull;
    private String pattern;

    @Override
    public void initialize(ColorConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
        pattern = constraintAnnotation.pattern();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return StringUtils.isBlank(value) ? allowNull : StringUtils.isNotBlank(value) && value.matches(pattern);
    }
}
