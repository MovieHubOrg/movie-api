package com.movie.api.validation;

import com.movie.api.validation.impl.GenderValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = GenderValidation.class)
@Documented
public @interface GenderConstraint {
    boolean allowNull() default false;

    String message() default "Gender is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
