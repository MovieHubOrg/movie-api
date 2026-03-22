package com.movie.api.validation;

import com.movie.api.validation.impl.MovieTypeValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MovieTypeValidation.class)
@Documented
public @interface MovieTypeConstraint {
    boolean allowNull() default false;

    String message() default "Movie type is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
