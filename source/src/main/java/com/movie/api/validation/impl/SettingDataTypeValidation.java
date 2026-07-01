package com.movie.api.validation.impl;

import com.movie.api.validation.SettingDataTypeConstraint;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Set;

public class SettingDataTypeValidation implements ConstraintValidator<SettingDataTypeConstraint, String> {
    private static final Set<String> ALLOWED_DATA_TYPES = Set.of(
            "Integer",
            "String",
            "Boolean",
            "Double",
            "RichText",
            "Select",
            "Upload",
            "List"
    );

    private boolean allowNull;

    @Override
    public void initialize(SettingDataTypeConstraint constraintAnnotation) {
        allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null && allowNull) {
            return true;
        }
        return value != null && ALLOWED_DATA_TYPES.contains(value);
    }
}
