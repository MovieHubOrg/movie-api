package com.movie.api.validation;

import com.movie.api.validation.impl.SourceTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SourceTypeValidation.class)
@Documented
public @interface SourceTypeConstraint {
    boolean allowNull() default false;

    String message() default "Source type is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
