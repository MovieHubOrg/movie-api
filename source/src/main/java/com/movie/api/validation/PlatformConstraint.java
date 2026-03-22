package com.movie.api.validation;

import com.movie.api.validation.impl.PlatformValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlatformValidation.class)
@Documented
public @interface PlatformConstraint {
    boolean allowNull() default false;

    String message() default "Platform is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
