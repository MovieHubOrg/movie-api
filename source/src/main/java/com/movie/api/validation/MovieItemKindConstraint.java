package com.movie.api.validation;

import com.movie.api.validation.impl.MovieItemKindValidation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MovieItemKindValidation.class)
@Documented
public @interface MovieItemKindConstraint {
    boolean allowNull() default false;

    String message() default "Movie item kind is invalid";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
