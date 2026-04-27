package com.movie.api.validation;

import com.movie.api.validation.impl.StartTimeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StartTimeValidator.class)
public @interface ValidStartTime {
    String message() default "startTime is required";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}