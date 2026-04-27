package com.movie.api.validation;

import com.movie.api.validation.impl.SettingDataTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SettingDataTypeValidation.class)
@Documented
public @interface SettingDataTypeConstraint {
    boolean allowNull() default false;

    String message() default "dataType must be one of: Integer, String, Boolean, Double, RichText, Select, Upload";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
